package controller;

import dao.CadastroUsersDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Set;
import model.CadastroUsuarioModel;

@WebServlet("/pages/cadastro")
public class CadastroController extends HttpServlet{

    private static final Set<String> FUNCOES_VALIDAS = Set.of("ADMIN", "GERENTE", "FUNCIONARIO", "VISITANTE");
    private static final LocalDate NASCIMENTO_MINIMO = LocalDate.of(1950, 1, 1);

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
              throws ServletException, IOException {

        String funcao = request.getParameter("funcao");

        if(funcao == null || !FUNCOES_VALIDAS.contains(funcao)) {
            response.sendRedirect("cadastro.html?erro=funcao_invalida");
            return;
        }

        try {
            LocalDate dtaNascimento = LocalDate.parse(request.getParameter("dtaNascimento"));
            if (dtaNascimento.isBefore(NASCIMENTO_MINIMO) || dtaNascimento.isAfter(LocalDate.now())) {
                response.sendRedirect("cadastro.html?erro=nascimento_invalido");
                return;
            }
        } catch (DateTimeParseException | NullPointerException e) {
            response.sendRedirect("cadastro.html?erro=nascimento_invalido");
            return;
        }

        CadastroUsuarioModel user = new CadastroUsuarioModel();
        
        user.setNome(request.getParameter("nameFirst"));
        user.setSobrenome(request.getParameter("sobreNome"));
        user.setMatricula(request.getParameter("matricula"));
        user.setCpf(request.getParameter("cpf"));
        user.setSexo(request.getParameter("opcao"));
        user.setDtaNascimento(request.getParameter("dtaNascimento"));   
        user.setEmail(request.getParameter("email"));
        user.setTelefone(request.getParameter("telefone"));
        user.setNomeUsuario(request.getParameter("usuario"));
        user.setSenha(request.getParameter("senha"));
        user.setFuncao(funcao);
        user.setCep(request.getParameter("cep"));
        user.setEndereco(request.getParameter("endereco"));
        user.setNumero(request.getParameter("numero"));
        user.setComplemento(request.getParameter("complemento"));
        user.setBairro(request.getParameter("bairro"));
        user.setCidade(request.getParameter("cidade"));
        user.setEstado(request.getParameter("estado"));

        CadastroUsersDAO dao = new CadastroUsersDAO();
        
        if(dao.cadastrar(user)) {
            response.sendRedirect("dashboard.html");
        }else{
            response.sendRedirect("cadastro.html");
        }
    }
}