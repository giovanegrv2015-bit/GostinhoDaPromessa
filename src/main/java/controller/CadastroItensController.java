package controller;

import dao.CadastroItensDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import model.CadastroItensModel;
import util.ValidadorItem;

@WebServlet("/cadastroItens")
public class CadastroItensController extends HttpServlet {

    private static final String PAGINA_FORMULARIO = "pages/cadastroItens.html";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        CadastroItensModel item = new CadastroItensModel();

        item.setCodigoBarras(request.getParameter("codigoBarras"));
        item.setNomeItem(request.getParameter("nomeItem"));
        item.setMarca(request.getParameter("marca"));
        item.setDataFabricacao(request.getParameter("dataFabricacao"));
        item.setDataVencimento(request.getParameter("dataVencimento"));
        item.setQuantidade(ValidadorItem.paraLong(request.getParameter("quantidade")));
        item.setValor(ValidadorItem.paraDecimal(request.getParameter("valor")));
        item.setStatus(request.getParameter("status"));
        item.setLocal(request.getParameter("local"));
        item.setCategoria(request.getParameter("categoria"));
        item.setEstoqueMinimo(ValidadorItem.paraLong(request.getParameter("estoqueMinimo")));
        // o campo "total" do formulário é só visual: quem calcula o total é o ValidadorItem

        String erro = ValidadorItem.validar(item);
        if (erro != null) {
            response.sendRedirect(PAGINA_FORMULARIO + "?erro=" + erro);
            return;
        }

        try {
            new CadastroItensDAO().salvar(item);
            response.sendRedirect("pages/dashboard.html?sucesso=item_cadastrado");
        } catch (SQLException e) {
            log("Falha ao salvar o item", e);
            response.sendRedirect(PAGINA_FORMULARIO + "?erro=erro_servidor");
        }
    }
}
