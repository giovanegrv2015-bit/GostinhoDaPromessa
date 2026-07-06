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
import java.util.Set;

@WebFilter("/*")
public class AuthFilter implements Filter{

    private static final Set<String> PERFIS_VALIDOS = Set.of("ADMIN", "GERENTE", "FUNCIONARIO");
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("Expires", "0");

        HttpSession session = req.getSession(false);
        
        String uri = req.getRequestURI();
        
        if(uri.contains("index.html") || uri.contains("login") || uri.contains("logout") || uri.contains("css") || uri.contains("js")){
                chain.doFilter(request, response);
                
                return;
            }
        
        if(session == null || session.getAttribute("usuario") == null) {
                res.sendRedirect(req.getContextPath() + "/index.html");
                return;
            }
        
        String perfil = (String) session.getAttribute("perfil");

        if (!PERFIS_VALIDOS.contains(perfil)) {
            boolean isDashboard = uri.equals(req.getContextPath() + "/pages/dashboard.html");
            if (!isDashboard) {
                res.sendRedirect(req.getContextPath() + "/pages/dashboard.html?acesso=negado");
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        boolean isCadastroFuncionarios =
                uri.equals(req.getContextPath() + "/pages/cadastro.html") ||
                uri.equals(req.getContextPath() + "/pages/cadastro");

        if(isCadastroFuncionarios && !("ADMIN".equals(perfil) || "GERENTE".equals(perfil))) {
            res.sendRedirect(req.getContextPath() + "/pages/dashboard.html?acesso=negado");
            return;
        }

        chain.doFilter(request, response);
    }
}