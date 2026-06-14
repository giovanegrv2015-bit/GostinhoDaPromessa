package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.CadastroItensModel;
import dao.CadastroItensDAO;

@WebServlet("/cadastroItens")
public class CadastroItensController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        CadastroItensModel item = new CadastroItensModel();

        item.setCodigoBarras(request.getParameter("codigoBarras"));
        item.setNomeItem(request.getParameter("nomeItem"));
        item.setFabricante(request.getParameter("fabricante"));
        item.setMarca(request.getParameter("marca"));
        item.setDataFabricacao(request.getParameter("dataFabricacao"));
        item.setDataVencimento(request.getParameter("dataVencimento"));
        item.setQuantidade(Long.parseLong(request.getParameter("quantidade")));
        item.setValor(request.getParameter("valor"));
        item.setTotal(request.getParameter("total"));
        item.setStatus(request.getParameter("status"));
        item.setLocal(request.getParameter("local"));
        item.setCategoria(request.getParameter("categoria"));
        item.setEstoqueMinimo(Long.parseLong(request.getParameter("estoqueMinimo")));

        CadastroItensDAO dao = new CadastroItensDAO();

        if (dao.salvar(item)) {
            response.sendRedirect("pages/dashboard.html");
        } else {
            response.sendRedirect("pages/cadastroItens.html");
        }
    }
}