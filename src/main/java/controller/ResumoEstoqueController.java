package controller;

import dao.CadastroItensDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import util.JsonUtil;

@WebServlet("/api/resumo")
public class ResumoEstoqueController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            JsonUtil.enviar(response, HttpServletResponse.SC_OK, new CadastroItensDAO().resumo());
        } catch (SQLException e) {
            log("Falha ao calcular o resumo do estoque", e);
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "erro_servidor");
        }
    }
}
