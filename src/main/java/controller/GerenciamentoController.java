package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.CadastroProdutoModel;
import java.io.BufferedReader;
import java.io.IOException;
import com.google.gson.Gson;
import dao.CadastroProdutosDAO;

@WebServlet("/api/gerenciamento")
public class GerenciamentoController extends HttpServlet {
    
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        String codigoBarras = request.getParameter("codigoBarras");

        if(codigoBarras == null || codigoBarras.isEmpty()){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\":\"codigoBarras é obrigatório\"}");
            return;
        }

        StringBuilder sb = new StringBuilder();
        try(BufferedReader reader = request.getReader()) {
            String linha;
            while ((linha = reader.readLine()) != null){
                sb.append(linha);
            }
        }

        CadastroProdutoModel produto = new Gson().fromJson(sb.toString(), CadastroProdutoModel.class);
        produto.setCodigoBarras(codigoBarras);
        
        CadastroProdutosDAO dao = new CadastroProdutosDAO();
        boolean sucesso = dao.atualizar(produto);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if(sucesso){
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"mensagem\":\"Produto atualizado com sucesso\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"erro\":\"Falha ao atualizar o produto\"}");
        }

    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
    throws IOException {

        String codigoBarras = request.getParameter("codigoBarras");

        if(codigoBarras == null || codigoBarras.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\":\"codigobarras é obrigatório\"}");
            return;
        }

        CadastroProdutosDAO dao = new CadastroProdutosDAO();
        boolean sucesso = dao.excluir(codigoBarras);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if(sucesso) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"mensagem\":\"Produto excluído com sucesso\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"erro\":\"Falha ao excluir o produto\"}");
        }
    }
    
}
