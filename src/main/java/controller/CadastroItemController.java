package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.CadastroItemModel;
import dao.CadastroItemDAO;

@WebServlet("/cadastroProdutos")
public class CadastroItemController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        CadastroItemModel produto = new CadastroItemModel();

        produto.setCodigoBarras(request.getParameter("codigoBarras"));
        produto.setNomeItem(request.getParameter("nomeItem"));
        produto.setFabricante(request.getParameter("fabricante"));
        produto.setMarca(request.getParameter("marca"));
        produto.setDataFabricacao(request.getParameter("dataFabricacao"));
        produto.setDataVencimento(request.getParameter("dataVencimento"));
        produto.setQuantidade(Long.parseLong(request.getParameter("quantidade")));
        produto.setValor(request.getParameter("valor"));
        produto.setTotal(request.getParameter("total"));
        produto.setStatus(request.getParameter("status"));

        CadastroItemDAO dao = new CadastroItemDAO();

        if (dao.salvar(produto)) {
            response.sendRedirect("pages/dashboard.html");
        } else {
            response.sendRedirect("pages/cadastroProdutos.html");
        }
    }
}