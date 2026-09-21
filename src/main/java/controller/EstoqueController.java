package controller;

import dao.CadastroItensDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import model.CadastroItensModel;
import util.JsonUtil;

@WebServlet("/api/estoque")
public class EstoqueController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String nome = request.getParameter("nome");
        String tipo = request.getParameter("tipo");
        String data = request.getParameter("data");

        if (data != null && !data.isBlank()) {
            try {
                LocalDate.parse(data);
            } catch (DateTimeParseException e) {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "data_invalida");
                return;
            }
        }

        try {
            List<CadastroItensModel> lista = new CadastroItensDAO().listarComFiltro(nome, tipo, data);
            JsonUtil.enviar(response, HttpServletResponse.SC_OK, lista);
        } catch (SQLException e) {
            // Antes o erro era engolido e a tela mostrava "nenhum item", como se o estoque estivesse vazio.
            log("Falha ao listar o estoque", e);
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "erro_servidor");
        }
    }
}
