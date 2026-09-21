package controller;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import model.UserModel;
import util.UsuariosIniciais;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String usuario = request.getParameter("users");
        String senha = request.getParameter("psw");

        try {
            UsuariosIniciais.garantir();
        } catch (SQLException e) {
            // Um ADMIN_USER mal configurado no .env não pode impedir o login de quem já existe.
            log("Não foi possível criar os usuários iniciais", e);
        }

        UserModel user;
        try {
            user = new UserDAO().validarLogin(usuario, senha);
        } catch (SQLException e) {
            log("Falha ao consultar o banco durante o login", e);
            response.sendRedirect(request.getContextPath() + "/index.html?erro=erro_servidor");
            return;
        }

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/index.html?erro=login_invalido");
            return;
        }

        // Descarta a sessão que existia antes do login e cria outra. Sem isso, quem
        // conseguisse plantar um id de sessão no navegador da vítima entraria junto
        // com ela depois do login (ataque de "session fixation").
        HttpSession antiga = request.getSession(false);
        if (antiga != null) {
            antiga.invalidate();
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("usuario", user.getUsername());
        session.setAttribute("perfil", user.getFuncao());

        response.sendRedirect(request.getContextPath() + "/pages/dashboard.html");
    }
}
