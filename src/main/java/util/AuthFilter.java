package util;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Ponto único de controle de acesso.
 *
 * Quem pode o quê:
 *   - sem login ............ tela de login, /login, /logout e arquivos estáticos (css, js, assets)
 *   - qualquer logado ...... dashboard e as APIs de leitura que o dashboard usa
 *   - ADMIN/GERENTE/FUNCIONARIO ... cadastro e gerenciamento de itens
 *   - ADMIN/GERENTE ........ cadastro de funcionários
 *
 * A versão anterior decidia com uri.contains("js"), uri.contains("css")... Isso
 * compara um PEDAÇO do texto em QUALQUER posição da URL. Como o Tomcat aceita
 * "parâmetros de caminho" (tudo depois de um ';'), /api/gerenciamento;js caía no
 * servlet de gerenciamento E passava pelo filtro sem login. Agora a decisão usa o
 * caminho que o próprio Tomcat resolveu (getServletPath), comparado por igualdade
 * ou por começo (startsWith), nunca por "contém".
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    private static final Set<String> PERFIS_OPERACAO = Set.of("ADMIN", "GERENTE", "FUNCIONARIO");
    private static final Set<String> PERFIS_GESTAO = Set.of("ADMIN", "GERENTE");

    private static final Set<String> CAMINHOS_PUBLICOS = Set.of("/", "/index.html", "/erro.html", "/login", "/logout");
    private static final List<String> PASTAS_PUBLICAS = List.of("/css/", "/js/", "/assets/");

    private static final Set<String> LIBERADO_PARA_QUALQUER_LOGADO =
            Set.of("/pages/dashboard.html", "/api/estoque", "/api/resumo", "/api/perfil");
    private static final Set<String> SOMENTE_GESTAO = Set.of("/pages/cadastro.html", "/pages/cadastro");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String caminho = caminhoResolvido(req);

        aplicarCabecalhosDeSeguranca(res);

        if (estaEmPastaPublica(caminho)) {
            // "no-cache" = pode guardar, mas pergunta ao servidor antes de usar (resposta 304, minúscula).
            // Antes css/js/imagens eram baixados inteiros de novo a cada clique.
            res.setHeader("Cache-Control", "no-cache");
            chain.doFilter(request, response);
            return;
        }

        // Páginas e APIs nunca ficam guardadas: depois do logout o botão "voltar" não mostra nada.
        res.setHeader("Cache-Control", "no-store");

        if (CAMINHOS_PUBLICOS.contains(caminho)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            if (ehApi(caminho)) {
                // fetch() não "vê" redirecionamento para o login; precisa de um status claro
                JsonUtil.enviarErro(res, HttpServletResponse.SC_UNAUTHORIZED, "sessao_expirada");
            } else {
                res.sendRedirect(req.getContextPath() + "/index.html");
            }
            return;
        }

        String perfil = (String) session.getAttribute("perfil");

        boolean permitido;
        if (LIBERADO_PARA_QUALQUER_LOGADO.contains(caminho)) {
            permitido = true;
        } else if (SOMENTE_GESTAO.contains(caminho)) {
            permitido = PERFIS_GESTAO.contains(perfil);
        } else {
            permitido = PERFIS_OPERACAO.contains(perfil);
        }

        if (!permitido) {
            if (ehApi(caminho)) {
                JsonUtil.enviarErro(res, HttpServletResponse.SC_FORBIDDEN, "negado");
            } else {
                res.sendRedirect(req.getContextPath() + "/pages/dashboard.html?acesso=negado");
            }
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * O caminho que o Tomcat usou para escolher o servlet: já decodificado, sem
     * contexto, sem query string e sem ";parametros". É o único valor confiável
     * para decidir permissão. getRequestURI() devolve o texto cru que o cliente mandou.
     */
    private static String caminhoResolvido(HttpServletRequest req) {
        String caminho = req.getServletPath();
        if (req.getPathInfo() != null) {
            caminho += req.getPathInfo();
        }
        return caminho;
    }

    private static boolean estaEmPastaPublica(String caminho) {
        for (String pasta : PASTAS_PUBLICAS) {
            if (caminho.startsWith(pasta)) {
                return true;
            }
        }
        return false;
    }

    private static boolean ehApi(String caminho) {
        return caminho.startsWith("/api/");
    }

    private static void aplicarCabecalhosDeSeguranca(HttpServletResponse res) {
        // O navegador só executa script, estilo e imagem vindos do próprio site. É a
        // segunda barreira contra XSS (a primeira é escapar o texto na hora de exibir).
        // connect-src libera o ViaCEP, usado no cadastro de funcionários.
        res.setHeader("Content-Security-Policy",
                "default-src 'self'; connect-src 'self' https://viacep.com.br; "
                + "frame-ancestors 'none'; form-action 'self'; base-uri 'self'");
        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("X-Frame-Options", "DENY");
        res.setHeader("Referrer-Policy", "same-origin");
    }
}
