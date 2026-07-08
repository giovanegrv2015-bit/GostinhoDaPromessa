package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.CadastroItensModel;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import com.google.gson.Gson;
import dao.CadastroItensDAO;

@WebServlet("/api/gerenciamento")
public class GerenciamentoController extends HttpServlet {

    private static final LocalDate FABRICACAO_MINIMA = LocalDate.of(2000, 1, 1);
    
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        String idParam = request.getParameter("id");

        int id;
        try {
            id = Integer.parseInt(idParam);
        }catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\":\"id é obrigatório e deve ser numérico\"}");
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
        item.setId(id);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        double valor;
        try {
            String valorNormalizado = item.getValor() != null ? item.getValor().replace(",", ".") : null;
            valor = Double.parseDouble(valorNormalizado);
        } catch (NumberFormatException | NullPointerException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\":\"Valor unitário inválido.\"}");
            return;
        }
        if (valor <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\":\"O valor unitário deve ser maior que zero.\"}");
            return;
        }

        boolean isEmbalagem = "Embalagens".equals(item.getCategoria());
        if (!isEmbalagem) {
            try {
                LocalDate dataFabricacao = LocalDate.parse(item.getDataFabricacao());
                LocalDate dataVencimento = LocalDate.parse(item.getDataVencimento());

                if (dataFabricacao.isAfter(LocalDate.now())) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"erro\":\"A data de fabricação não pode ser no futuro.\"}");
                    return;
                }
                if (dataFabricacao.isBefore(FABRICACAO_MINIMA)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"erro\":\"A data de fabricação não pode ser anterior a 2000.\"}");
                    return;
                }
                if (dataFabricacao.isAfter(dataVencimento)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"erro\":\"A data de fabricação não pode ser posterior à data de vencimento.\"}");
                    return;
                }
            } catch (DateTimeParseException | NullPointerException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"erro\":\"Datas inválidas.\"}");
                return;
            }
        } else {
            item.setDataVencimento(null);
            if (item.getDataFabricacao() != null && !item.getDataFabricacao().isBlank()) {
                try {
                    LocalDate dataFabricacao = LocalDate.parse(item.getDataFabricacao());
                    if (dataFabricacao.isAfter(LocalDate.now())) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"erro\":\"A data de fabricação não pode ser no futuro.\"}");
                        return;
                    }
                    if (dataFabricacao.isBefore(FABRICACAO_MINIMA)) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"erro\":\"A data de fabricação não pode ser anterior a 2000.\"}");
                        return;
                    }
                } catch (DateTimeParseException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"erro\":\"Data de fabricação inválida.\"}");
                    return;
                }
            }
        }

        CadastroItensDAO dao = new CadastroItensDAO();
        boolean sucesso = dao.atualizar(item);

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

        String idParam = request.getParameter("id");

        int id;
        try {
            id = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"erro\":\"id é obrigatório e deve ser numérico\"}");
            return;
        }

        CadastroItensDAO dao = new CadastroItensDAO();
        boolean sucesso = dao.excluir(id);

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