package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.CadastroItensModel;
import java.io.BufferedReader;
import java.io.IOException;
import com.google.gson.Gson;
import dao.CadastroItensDAO;

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

        CadastroItensModel item = new Gson().fromJson(sb.toString(), CadastroItensModel.class);
        item.setCodigoBarras(codigoBarras);
        
        CadastroItensDAO dao = new CadastroItensDAO();
        boolean sucesso = dao.atualizar(item);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if(sucesso){
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"mensagem\":\"Item atualizado com sucesso\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"erro\":\"Falha ao atualizar o item\"}");
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

        CadastroItensDAO dao = new CadastroItensDAO();
        boolean sucesso = dao.excluir(codigoBarras);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if(sucesso) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"mensagem\":\"Item excluído com sucesso\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"erro\":\"Falha ao excluir o item\"}");
        }
    }
    
}