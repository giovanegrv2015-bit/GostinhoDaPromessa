package controller;

import com.google.gson.JsonParseException;
import dao.CadastroItensDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import model.CadastroItensModel;
import util.JsonUtil;
import util.ValidadorItem;

@WebServlet("/api/gerenciamento")
public class GerenciamentoController extends HttpServlet {

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Integer id = lerId(request);
        if (id == null) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id_invalido");
            return;
        }

        CadastroItensModel item;
        try {
            // o Gson lê direto do corpo da requisição; não precisa montar a String na mão
            item = JsonUtil.GSON.fromJson(request.getReader(), CadastroItensModel.class);
        } catch (JsonParseException e) {
            // JSON quebrado ou número que não é número (ex.: "quantidade":"abc")
            item = null;
        }
        if (item == null) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "numero_invalido");
            return;
        }
        item.setId(id);

        // o código de barras não é editável nesta tela, por isso não é exigido aqui
        String erro = ValidadorItem.validar(item, false);
        if (erro != null) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, erro);
            return;
        }

        try {
            if (new CadastroItensDAO().atualizar(item)) {
                JsonUtil.enviar(response, HttpServletResponse.SC_OK, Map.of("mensagem", "item_atualizado"));
            } else {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "item_nao_encontrado");
            }
        } catch (SQLException e) {
            log("Falha ao atualizar o item " + id, e);
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "erro_servidor");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Integer id = lerId(request);
        if (id == null) {
            JsonUtil.enviarErro(response, HttpServletResponse.SC_BAD_REQUEST, "id_invalido");
            return;
        }

        try {
            if (new CadastroItensDAO().excluir(id)) {
                JsonUtil.enviar(response, HttpServletResponse.SC_OK, Map.of("mensagem", "item_excluido"));
            } else {
                JsonUtil.enviarErro(response, HttpServletResponse.SC_NOT_FOUND, "item_nao_encontrado");
            }
        } catch (SQLException e) {
            log("Falha ao excluir o item " + id, e);
            JsonUtil.enviarErro(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "erro_servidor");
        }
    }

    /** Lê o ?id= da URL. Devolve null se não veio ou não é número. */
    private Integer lerId(HttpServletRequest request) {
        try {
            return Integer.valueOf(request.getParameter("id"));
        } catch (NumberFormatException e) {
            return null; // Integer.valueOf(null) também cai aqui
        }
    }
}
