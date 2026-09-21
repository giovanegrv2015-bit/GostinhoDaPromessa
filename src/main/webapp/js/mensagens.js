// Dicionário único de mensagens. O servidor manda só o CÓDIGO (?erro=codigo na URL
// ou {"erro":"codigo"} no JSON); o texto que o usuário lê mora aqui.
const MENSAGENS = {

    // login
    "login_invalido": "Usuário ou senha incorretos. Tente novamente.",
    "sessao_expirada": "Sua sessão expirou. Entre novamente.",

    // permissão
    "negado": "Você não tem permissão para acessar essa área.",

    // cadastro de funcionário
    "funcao_invalida": "Função inválida. Selecione uma opção da lista.",
    "nascimento_invalido": "Data de nascimento inválida (deve ser entre 1950 e hoje).",
    "usuario_invalido": "Nome de usuário inválido. Use apenas letras, sem espaços, números ou símbolos.",
    "usuario_existente": "Já existe um funcionário com esse nome de usuário. Escolha outro.",
    "senha_invalida": "A senha deve ter entre 8 e 72 caracteres.",
    "texto_longo": "Um dos campos de texto passou do tamanho permitido.",

    // cadastro e edição de item
    "campos_obrigatorios": "Preencha o nome do item e o código de barras.",
    "texto_invalido": "Use apenas letras e números (sem símbolos ou emojis), com no máximo 100 caracteres.",
    "opcao_invalida": "Selecione status, local e categoria a partir das listas.",
    "numero_invalido": "Preencha corretamente os campos numéricos (quantidade, valor com até 2 casas decimais e estoque mínimo).",
    "valor_invalido": "O valor unitário deve ser maior que zero.",
    "data_invalida": "Datas inválidas: informe fabricação e vencimento, e a fabricação não pode ser posterior ao vencimento.",
    "data_futura": "A data de fabricação não pode ser no futuro.",
    "data_fabricacao_antiga": "A data de fabricação não pode ser anterior a 2000.",
    "id_invalido": "Item inválido. Recarregue a página e tente de novo.",
    "item_nao_encontrado": "Este item não existe mais. Recarregue a página.",

    // genérico
    "erro_servidor": "Não foi possível concluir a operação. Tente novamente em instantes.",

    // sucesso
    "item_cadastrado": "Item cadastrado com sucesso.",
    "item_atualizado": "Item atualizado com sucesso.",
    "item_excluido": "Item excluído com sucesso.",
    "funcionario_cadastrado": "Funcionário cadastrado com sucesso.",
};

// Texto de um código; se o código for desconhecido, cai na mensagem genérica.
function textoDaMensagem(codigo) {
    return MENSAGENS[codigo] || MENSAGENS["erro_servidor"];
}

// tipo: "erro" (vermelho) ou "sucesso" (verde). Só existe um banner por vez.
function mostrarBanner(texto, tipo = "erro") {
    document.querySelectorAll(".banner-mensagem").forEach(antigo => antigo.remove());

    const banner = document.createElement("div");
    banner.className = "banner-mensagem" + (tipo === "sucesso" ? " banner-sucesso" : "");
    banner.setAttribute("role", "alert");
    banner.textContent = texto; // textContent nunca interpreta HTML
    document.body.prepend(banner);

    // some sozinho (erro fica mais tempo na tela) ou com um clique
    banner.addEventListener("click", () => banner.remove());
    setTimeout(() => banner.remove(), tipo === "sucesso" ? 5000 : 10000);
}

// Lê ?erro=, ?acesso= ou ?sucesso= da URL, mostra o banner e limpa a URL.
function exibirMensagem() {
    const params = new URLSearchParams(window.location.search);
    const codigoErro = params.get("erro");
    const codigoAcesso = params.get("acesso");
    const codigoSucesso = params.get("sucesso");

    if (codigoAcesso === "negado") {
        mostrarBanner(MENSAGENS["negado"]);
    } else if (codigoErro && MENSAGENS[codigoErro]) {
        mostrarBanner(MENSAGENS[codigoErro]);
    } else if (codigoSucesso && MENSAGENS[codigoSucesso]) {
        mostrarBanner(MENSAGENS[codigoSucesso], "sucesso");
    } else {
        return;
    }

    params.delete("erro");
    params.delete("acesso");
    params.delete("sucesso");
    const novaUrl = window.location.pathname + (params.toString() ? "?" + params.toString() : "");
    window.history.replaceState({}, "", novaUrl);
}

document.addEventListener("DOMContentLoaded", exibirMensagem);
