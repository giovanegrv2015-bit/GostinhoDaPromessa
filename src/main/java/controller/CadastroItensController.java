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

    private static final LocalDate FABRICACAO_MINIMA = LocalDate.of(2000, 1, 1);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

                long quantidade;
                long estoqueMinimo;
                double valor;
                String valorNormalizado = request.getParameter("valor") != null
                        ? request.getParameter("valor").replace(",", ".") : null;

                try{
                    quantidade = Long.parseLong(request.getParameter("quantidade"));
                    estoqueMinimo = Long.parseLong(request.getParameter("estoqueMinimo"));
                    valor = Double.parseDouble(valorNormalizado);
                }catch (NumberFormatException | NullPointerException e) {
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

                        if (dataFabricacao.isAfter(LocalDate.now())) {
                            response.sendRedirect("pages/cadastroItens.html?erro=data_futura");
                            return;
                        }

                        if (dataFabricacao.isBefore(FABRICACAO_MINIMA)) {
                            response.sendRedirect("pages/cadastroItens.html?erro=data_fabricacao_antiga");
                            return;
                        }

                        if (dataFabricacao.isAfter(dataVencimento)) {
                            response.sendRedirect("pages/cadastroItens.html?erro=data_invalida");
                            return;
                        }
                    } catch (DateTimeParseException | NullPointerException e) {
                        response.sendRedirect("pages/cadastroItens.html?erro=data_invalida");
                        return;
                    }
                } else {
                    String dataFabricacaoParam = request.getParameter("dataFabricacao");
                    if (dataFabricacaoParam != null && !dataFabricacaoParam.isBlank()) {
                        try {
                            LocalDate dataFabricacao = LocalDate.parse(dataFabricacaoParam);
                            if (dataFabricacao.isAfter(LocalDate.now())) {
                                response.sendRedirect("pages/cadastroItens.html?erro=data_futura");
                                return;
                            }
                            if (dataFabricacao.isBefore(FABRICACAO_MINIMA)) {
                                response.sendRedirect("pages/cadastroItens.html?erro=data_fabricacao_antiga");
                                return;
                            }
                        } catch (DateTimeParseException e) {
                            response.sendRedirect("pages/cadastroItens.html?erro=data_invalida");
                            return;
                        }
                    }
                }

        CadastroItensModel item = new CadastroItensModel();

        item.setCodigoBarras(request.getParameter("codigoBarras"));
        item.setNomeItem(request.getParameter("nomeItem"));
        item.setFabricante(request.getParameter("fabricante"));
        item.setMarca(request.getParameter("marca"));
        item.setDataFabricacao(request.getParameter("dataFabricacao"));
        item.setDataVencimento(isEmbalagem ? null : request.getParameter("dataVencimento"));
        item.setQuantidade(quantidade);
        item.setValor(valorNormalizado);
        item.setTotal(request.getParameter("total"));
        item.setStatus(request.getParameter("status"));
        item.setLocal(request.getParameter("local"));
        item.setCategoria(categoria);
        item.setEstoqueMinimo(estoqueMinimo);

        CadastroItensDAO dao = new CadastroItensDAO();

        if (dao.salvar(item)) {
            response.sendRedirect("pages/dashboard.html");
        } else {
            response.sendRedirect("pages/cadastroItens.html");
        }
    }   
}