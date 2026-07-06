package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import model.CadastroItensModel;
import dao.CadastroItensDAO;

@WebServlet("/cadastroItens")
public class CadastroItensController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

                long quantidade;
                long estoqueMinimo;
                double valor;

                try{
                    quantidade = Long.parseLong(request.getParameter("quantidade"));
                    estoqueMinimo = Long.parseLong(request.getParameter("estoqueMinimo"));
                    valor = Double.parseDouble(request.getParameter("valor"));
                }catch (NumberFormatException e) {
                    response.sendRedirect("pages/cadastroItens.html?erro=numero_invalido");
                    return;
                }

                if (valor <= 0) {
                    response.sendRedirect("pages/cadastroItens.html?erro=valor_invalido");
                    return;
                }

                String categoria = request.getParameter("categoria");
                boolean isEmbalagem= "Embalagens".equals(categoria);

                if(!isEmbalagem) {
                    try{
                        LocalDate dataFabricacao = LocalDate.parse(request.getParameter("dataFabricacao"));
                        LocalDate dataVencimento = LocalDate.parse(request.getParameter("dataVencimento"));

                        if (dataFabricacao.isAfter(dataVencimento)) {
                            response.sendRedirect("pages/cadastroItens.html?erro=data_invalida");
                            return;
                        }
                    } catch (DateTimeParseException | NullPointerException e) {
                        response.sendRedirect("pages/cadastroItens.html?erro=data_invalida");
                        return;
                    }
                }

        CadastroItensModel item = new CadastroItensModel();

        item.setCodigoBarras(request.getParameter("codigoBarras"));
        item.setNomeItem(request.getParameter("nomeItem"));
        item.setFabricante(request.getParameter("fabricante"));
        item.setMarca(request.getParameter("marca"));
        item.setDataFabricacao(request.getParameter("dataFabricacao"));
        item.setDataVencimento(request.getParameter("dataVencimento"));
        item.setQuantidade(quantidade);
        item.setValor(request.getParameter("valor"));
        item.setTotal(request.getParameter("total"));
        item.setStatus(request.getParameter("status"));
        item.setLocal(request.getParameter("local"));
        item.setCategoria(request.getParameter("categoria"));
        item.setEstoqueMinimo(estoqueMinimo);

        CadastroItensDAO dao = new CadastroItensDAO();

        if (dao.salvar(item)) {
            response.sendRedirect("pages/dashboard.html");
        } else {
            response.sendRedirect("pages/cadastroItens.html");
        }
    }
}