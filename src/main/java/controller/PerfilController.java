package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import util.JsonUtil;

@WebServlet("/api/perfil")
public class PerfilController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession(false);
        String perfil = (session != null) ? (String) session.getAttribute("perfil") : null;

        JsonUtil.enviar(response, HttpServletResponse.SC_OK, Map.of("perfil", perfil != null ? perfil : ""));
    }
}
