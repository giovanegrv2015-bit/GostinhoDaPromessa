# Changelog

## v1.1 — setembro/2026

A v1.0 é a versão apresentada e aprovada no SENAI em 08/07/2026 (commit `4af53c6`).
Para guardá-la intacta, crie a tag antes de commitar a v1.1:

    git tag v1.0-senai 4af53c6
    git push origin v1.0-senai

Depois disso `git checkout v1.0-senai` devolve o projeto exatamente como foi apresentado, para sempre.

Nenhuma funcionalidade foi removida. Foram três frentes: segurança (o que impedia pôr o
sistema na internet), bugs, e limpeza de código morto ou duplicado.

### Como isto foi testado, e o que NÃO foi

Testado rodando de verdade: Tomcat 10.1 + MySQL 8.0, 126 verificações por HTTP (curl) e
53 de interface (Chromium automatizado), com banco novo e também com o banco antigo
(usuários demo do `init.sql` anterior). Os problemas de segurança abaixo foram
reproduzidos na v1.0 antes de serem corrigidos.

Não testado: o build pelo Maven/Docker. O ambiente onde os testes rodaram não tem acesso
ao Maven Central nem ao Docker Hub, então o código foi compilado direto com `javac`
contra as mesmas versões de Gson e jBCrypt, e o driver usado foi o 8.1.0 (não o 8.4.0 do
`pom.xml` novo). As três mudanças do `pom.xml` precisam ser confirmadas na sua máquina com
`docker compose up -d --build`; o GitHub Actions confirma de novo no push.

---

### 1. Segurança

**1.1 Qualquer pessoa, sem login, conseguia ler, apagar e criar administrador** — `util/AuthFilter.java`

O filtro liberava a requisição se a URL *contivesse* `js`, `css`, `login`, `logout` ou
`index.html` (`uri.contains(...)`). O Tomcat aceita "parâmetros de caminho" — tudo depois
de um `;` — e os ignora na hora de escolher o servlet. Resultado, reproduzido na v1.0:

| Requisição, sem estar logado | O que acontecia |
|---|---|
| `GET /api/estoque;js` | devolvia o estoque inteiro |
| `DELETE /api/gerenciamento;js?id=2` | apagava o item |
| `POST /pages/cadastro;css` com `funcao=ADMIN` | criava um administrador, que depois logava normalmente |

Pelo mesmo motivo, um FUNCIONARIO criava ADMIN com `/pages/cadastro;x` (a regra usava
`uri.equals`, e a URL com `;x` não era "igual").

Agora a decisão usa `getServletPath()` — o caminho que o próprio Tomcat resolveu, já sem
`;parametros`, sem `..` e decodificado — comparado por igualdade ou por `startsWith`, nunca
por "contém". As regras de perfil são as mesmas de antes.

**1.2 Senhas públicas** — `db/init.sql`, `util/UsuariosIniciais.java`, `.env.example`, `docker-compose.yml`

`admin/admin1234` e os três usuários `demo1234` estavam no `init.sql`, que é público no
GitHub. O `init.sql` agora só cria as tabelas. No primeiro login o sistema cria o
administrador a partir de `ADMIN_USER` e `ADMIN_PASSWORD` do `.env` (mínimo 8 caracteres),
se ainda não existir. Os usuários demo só são criados com `SEED_DEMO=true`.

Roda no primeiro login, e não na subida do Tomcat, porque na subida o MySQL ainda pode
estar inicializando. Se o `.env` estiver mal configurado, o erro vai para o log e o login
de quem já existe continua funcionando.

**1.3 XSS armazenado** — `js/util.js`, `js/dashboard.js`, `js/gerenciamento.js`, `util/ValidadorItem.java`

O nome do item ia para a tela por `innerHTML` sem tratamento, e o servidor só validava
texto pelo atributo `pattern` do HTML, que não vale para quem manda a requisição direto.
Um item chamado `<img src=x onerror=...>` executava código no navegador de quem abrisse o
dashboard (reproduzido). Correção em três camadas: `escaparHtml()` em todo texto vindo do
banco; o servidor passou a validar os mesmos padrões do HTML; e o cabeçalho
`Content-Security-Policy` faz o navegador recusar script que não venha do próprio site.
Por causa do CSP, os `onclick="..."` gerados dentro do HTML viraram um único ouvinte de
evento na lista (delegação), e o `style="display:none"` do modal virou o atributo `hidden`.

**1.4 Cadastro de funcionário aceitava senha vazia e nome repetido** — `controller/CadastroController.java`, `dao/UserDAO.java`, `db/init.sql`

Reproduzido: foi possível criar um segundo usuário `admin`, com senha vazia. Agora a senha
tem de 8 a 72 caracteres (72 é o limite do BCrypt), o nome de usuário é conferido antes do
`INSERT`, e a tabela ganhou `UNIQUE (username)` — que só vale para bancos criados a partir
de agora.

**1.5 Sessão e cookies** — `controller/LoginServlet.java`, `WEB-INF/web.xml`, `META-INF/context.xml`

- O login descarta a sessão anterior e cria outra (evita "session fixation").
- Cookie de sessão com `HttpOnly` e `SameSite=Lax` (o navegador não o envia em POST vindo de outro site: defesa contra CSRF).
- Id de sessão só em cookie, nunca na URL.
- O `path="/Sistema"` do `context.xml` era ignorado pelo Tomcat e foi removido.

**1.6 Porta do banco aberta para a rede** — `docker-compose.yml`

`3307:3306` publica o MySQL (usuário root) em todas as interfaces; num servidor na internet
isso é o banco exposto. Agora é `127.0.0.1:3307:3306`: o IntelliJ continua conectando em
`localhost:3307`, mais ninguém.

**1.7 Página de erro própria** — `erro.html`, `WEB-INF/web.xml`

A página padrão do Tomcat mostra versão do servidor e stack trace para qualquer visitante.
O erro completo continua no log (`docker compose logs app`).

**1.8 Driver MySQL com vulnerabilidade conhecida** — `pom.xml`

8.0.33 (Maven) e 8.1.0 (o `.jar` commitado) têm a CVE-2023-22102, corrigida na 8.2.0.
Passou para 8.4.0, a linha LTS.

---

### 2. Bugs

| # | O que acontecia | Causa | Onde |
|---|---|---|---|
| 2.1 | VISITANTE via o dashboard vazio e com todos os botões do menu | O filtro redirecionava as próprias APIs do dashboard (`/api/estoque`, `/api/resumo`, `/api/perfil`). O `fetch` recebia HTML no lugar de JSON, dava erro, e o código que esconde os botões nem rodava | `AuthFilter.java` |
| 2.2 | "undefined" nas datas de embalagens | O Gson omite campo nulo; o JS imprimia `undefined` | `util.js` (`formatarData`) |
| 2.3 | Item com quantidade 0 abria o modal com o campo vazio e não salvava | `item.quantidade \|\| ''` — em JS, `0 \|\| ''` dá `''`. Trocado por `??` | `gerenciamento.js` |
| 2.4 | Seletor de página e botões Voltar/Próximo não faziam nada | Não existia JS para eles | `dashboard.js` (10 por página) |
| 2.5 | O total gravado era o que o navegador mandasse (2 × 1,00 com `total=999999` gravava 999999,00) | O servidor confiava no campo `total` do formulário. Agora o total é sempre quantidade × valor, calculado no servidor | `ValidadorItem.java` |
| 2.6 | Depois das 21h a "data de hoje" já era a de amanhã | `new Date().toISOString()` e `LocalDate.now()` num servidor em UTC. Agora: data local no JS e fuso `America/Bahia` explícito no Java | `util.js`, `ValidadorItem.java` |
| 2.7 | Banco fora do ar virava `NullPointerException`, e o dashboard mostrava "nenhum item" | `getConnection()` devolvia `null`; o DAO engolia o erro e devolvia lista vazia. Agora o erro sobe como `SQLException` e a API responde 500 com `{"erro":"erro_servidor"}` | `ConnectionFactory.java`, DAOs, controllers |
| 2.8 | Sessão expirada deixava a tela vazia, sem explicação | O `fetch` seguia o redirecionamento para o login e tentava ler HTML como JSON. Agora a API responde 401 e o JS volta ao login com aviso | `AuthFilter.java`, `util.js` |
| 2.9 | Quantidade ausente no PUT virava 0 em silêncio; quantidade negativa era aceita | Campo `long` (primitivo) assume 0. Virou `Long`, e o validador exige valor ≥ 0 | `CadastroItensModel.java`, `ValidadorItem.java` |
| 2.10 | `valor=1e5` era gravado como 100000,00; `5.999` virava 6,00 sem avisar; `NaN` falhava sem mensagem | `Double.parseDouble` aceita tudo isso, e a conversão ficava por conta do MySQL. Agora `BigDecimal` com no máximo 2 casas e o limite do `DECIMAL(10,2)` | `ValidadorItem.java` |
| 2.11 | Excluir ou editar id inexistente respondia 500 | Agora 404 `item_nao_encontrado` | `GerenciamentoController.java` |
| 2.11b | PUT com JSON quebrado respondia 500 com o stack trace no corpo da resposta | `fromJson` lançava exceção sem tratamento. Agora responde 400 | `GerenciamentoController.java` |
| 2.12 | O cadastro de funcionário recusava endereços reais | Os `pattern` não aceitavam apóstrofo, parênteses, barra: "Dias d'Ávila", "Centro (Distrito)" e o complemento "de 3205/3206 ao fim" que o próprio ViaCEP preenche | `cadastro.html` |
| 2.13 | A validação do telefone nunca funcionou | `[0-9()\-\s]+` é regex inválida no modo que os navegadores atuais usam para `pattern` (parênteses sem escape dentro de `[]`); o navegador descartava a regra calado | `cadastro.html` |
| 2.14 | Os `pattern` do modal de edição eram enfeite | Os campos não estavam dentro de um `<form>`. Agora estão, e o botão Salvar chama `reportValidity()` | `gerenciamento.html`, `gerenciamento.js` |
| 2.15 | A busca do gerenciamento podia mostrar resultado errado | Uma requisição por tecla, e as respostas podiam chegar fora de ordem. Agora filtra a lista já carregada, sem ir ao servidor | `gerenciamento.js` |
| 2.16 | Falha ao carregar os itens aparecia sem estilo | O JS usava a classe `erro-msg` e o CSS definia `error-msg` | `gerenciamento.css` |
| 2.17 | Enter no CEP consultava o ViaCEP duas vezes | Uma no Enter, outra no `blur` | `cep.js` |
| 2.18 | Falha ao cadastrar voltava ao formulário sem mensagem nenhuma | Redirecionava sem `?erro=`. Agora todo caminho de erro tem código e todo sucesso tem banner verde | controllers, `mensagens.js` |
| 2.19 | No celular a tabela do dashboard era cortada | `overflow: hidden`. Agora rola para o lado | `dashboard.css` |
| 2.20 | O healthcheck do MySQL passava cedo demais | `-h localhost` usa socket e responde durante a inicialização temporária do MySQL, quando ele ainda não aceita o app. Agora `-h 127.0.0.1` (TCP) | `docker-compose.yml` |
| 2.21 | `docker-compose.yml` do GitHub apontava para `/home/giovanedocker/...` | A correção `./db:/docker-entrypoint-initdb.d` existia só na sua máquina, sem commit | `docker-compose.yml` |

---

### 3. Código morto e duplicado

**Removido**

| Arquivo | Por quê |
|---|---|
| `model/ProdutoModel.java` | 100% comentado |
| `com/ds/sistema/JakartaRestConfiguration.java` e `resources/JakartaEE11Resource.java` | Modelo do NetBeans para JAX-RS. O Tomcat não tem JAX-RS: nunca rodaram |
| `WEB-INF/beans.xml`, `resources/META-INF/persistence.xml`, `nb-configuration.xml` | CDI, JPA e NetBeans: o projeto não usa nenhum |
| `WEB-INF/lib/mysql-connector-j-8.1.0.jar` | O Maven já trazia o driver; o `.war` saía com dois drivers diferentes (e 2,4 MB de binário no Git) |
| `github/workFlow/ci-cd.yml` | Cópia de `.github/workflows/main.yml` numa pasta que o GitHub não lê |
| `js/filtro.js` | Fundido no `dashboard.js` |
| `js/logout.js` | O botão Sair virou um link comum: `<a href="../logout">` |
| `model/CadastroUsuarioModel.java`, `dao/CadastroUsersDAO.java` | Fundidos em `UserModel` e `UserDAO` |

**Unificado**

- `dashboard.js` e `filtro.js` desenhavam a mesma tabela com código copiado, cada um formatando valores de um jeito. Agora: uma função busca, uma desenha.
- A validação de item estava copiada no `CadastroItensController` e no `GerenciamentoController` (~80 linhas cada). Agora os dois chamam `ValidadorItem.validar()`. Regra copiada em dois lugares um dia muda em um e não no outro.
- Dois models e dois DAOs para a mesma tabela `users` viraram um de cada.
- O bloco de parâmetros repetido no `INSERT` e no `UPDATE` de itens virou `preencherCamposEditaveis()`.
- Escrever JSON na resposta estava repetido em 4 controllers, e o `PerfilController` montava JSON concatenando String. Agora `JsonUtil.enviar()`.
- O SQL do resumo saiu do controller e foi para o DAO, que é o padrão do resto do projeto.
- O servidor devolve só o código do erro; o texto fica no `mensagens.js`. Antes as mensagens do PUT estavam em Java e as do POST em JS.
- `body`, cabeçalho e menu estavam copiados em 3 arquivos CSS: viraram `css/layout.css`. A classe `.btn-menu`, que nenhum HTML usava, foi removida.
- `cep.js` tinha dois blocos `DOMContentLoaded` no mesmo campo.

**Otimizado**

- CSS, JS e imagens eram baixados inteiros a cada clique (`no-store` em tudo). Agora o navegador guarda e só revalida (resposta 304, sem corpo). Páginas e APIs continuam sem cache.
- `innerHTML +=` dentro do laço refazia a tabela inteira a cada item; agora a tabela é montada uma vez.
- `SELECT *` virou lista de colunas, com `ORDER BY` (sem ele a ordem não é garantida e a paginação ficaria instável).
- Datas vão e voltam do banco como `LocalDate`, que não carrega fuso.
- Dinheiro é `BigDecimal` do formulário até o banco (antes: `String` no model, `double` na validação).
- `pom.xml`: só a API de Servlets (`jakarta.servlet-api` 6.1.0) no lugar da API completa do Jakarta EE em versão de teste (`11.0.0-M1`).
- `.dockerignore`: o build não envia mais `.git`, `target` e `.env` para o Docker.

---

### 4. Pedidos extras

- **Logo na tela de login.** O `logo-gostinho.png` estava no projeto sem uso; antes não carregaria, porque o filtro bloqueava `/assets` para quem não estava logado.
- **Paginação do dashboard**, 10 itens por página.

Mudanças pequenas de tela: datas em dd/mm/aaaa, valores em R$, status "Entrada"/"Saída", o
filtro "Data" passou a se chamar "Vencimento" (é o que ele sempre filtrou), campo Total
somente leitura, mensagens em banner flutuante, link Sair no cadastro de funcionários.

---

### 5. Notas de atualização

1. Acrescente ao `.env` as três variáveis novas (modelo em `.env.example`): `ADMIN_USER`, `ADMIN_PASSWORD`, `SEED_DEMO`. Sem elas o sistema sobe, mas não cria administrador nenhum (fica um aviso no log).
2. `docker compose up -d --build`. É aqui que o `pom.xml` novo é validado de verdade.
3. Um banco que já existia continua como estava: os usuários demo antigos seguem nele, e o `UNIQUE (username)` não é aplicado. Para recriar no modelo novo: `docker compose down -v` (apaga os dados) e subir de novo.
4. Em produção: `SEED_DEMO=false` e senha forte em `ADMIN_PASSWORD`. Trocar `ADMIN_PASSWORD` depois não altera a senha de um usuário que já foi criado.

### 6. Fora do escopo desta versão

- **O modelo de dados.** A tabela `itens` mistura o item com a movimentação: não existe histórico de entrada e saída, o "status" vale para a linha inteira, e o total do dashboard é a soma de linhas "entrada" menos linhas "saida". Isso não é bug de código; é o desenho, e é o escopo definido para a v2.
- **Antes de abrir na internet:** HTTPS (sem ele a senha trafega em texto puro), usuário do MySQL que não seja root, backup do volume do banco, e limite de tentativas de login.
- O cadastro de funcionário guarda CPF, endereço e nascimento. Se só a sua irmã vai usar, vale perguntar se esses dados precisam existir (LGPD).
- Não há testes automatizados no repositório. `ValidadorItem` é uma classe sem dependências, boa candidata a um primeiro teste com JUnit.
