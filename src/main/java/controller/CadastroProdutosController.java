package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.CadastroProdutoModel;
import dao.CadastroProdutosDAO;

@WebServlet("/cadastroProdutos")
public class CadastroProdutosController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        CadastroProdutoModel produto = new CadastroProdutoModel();

        produto.setCodigoBarras(request.getParameter("codigoBarras"));
        produto.setNomeProduto(request.getParameter("nomeProduto"));
        produto.setFabricante(request.getParameter("fabricante"));
        produto.setMarca(request.getParameter("marca"));
        produto.setDataFabricacao(request.getParameter("dataFabricacao"));
        produto.setDataVencimento(request.getParameter("dataVencimento"));
        produto.setQuantidade(Long.parseLong(request.getParameter("quantidade")));
        produto.setValor(request.getParameter("valor"));
        produto.setTotal(request.getParameter("total"));
        produto.setStatus(request.getParameter("status"));

        CadastroProdutosDAO dao = new CadastroProdutosDAO();

        if (dao.salvar(produto)) {
            response.sendRedirect("pages/dashboard.html");
        } else {
            response.sendRedirect("pages/cadastroProdutos.html");
        }
    }
}