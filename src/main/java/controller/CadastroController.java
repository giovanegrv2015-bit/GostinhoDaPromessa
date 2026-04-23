package controller;

import dao.CadastroUsersDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.CadastroUsuarioModel;

@WebServlet("/pages/cadastro")
public class CadastroController extends HttpServlet{
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
              throws ServletException, IOException {
              
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
        user.setFuncao(request.getParameter("funcao"));
        user.setCep(request.getParameter("cep"));
        user.setEndereco(request.getParameter("endereco"));
        user.setNumero(Long.parseLong(request.getParameter("numero")));
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