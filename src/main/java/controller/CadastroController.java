package controller;

import dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.regex.Pattern;
import model.UserModel;
import util.ValidadorItem;

@WebServlet("/pages/cadastro")
public class CadastroController extends HttpServlet {

    private static final Set<String> FUNCOES_VALIDAS = Set.of("ADMIN", "GERENTE", "FUNCIONARIO", "VISITANTE");
    private static final LocalDate NASCIMENTO_MINIMO = LocalDate.of(1950, 1, 1);
    private static final Pattern USUARIO_VALIDO = Pattern.compile("[A-Za-zÀ-ÿ]{1,100}"); // mesmo pattern do HTML
    private static final int SENHA_MINIMA = 8;
    private static final int SENHA_MAXIMA = 72; // o BCrypt ignora em silêncio tudo que passa de 72 bytes

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String funcao = request.getParameter("funcao");
        if (funcao == null || !FUNCOES_VALIDAS.contains(funcao)) {
            response.sendRedirect("cadastro.html?erro=funcao_invalida");
            return;
        }

        String usuario = request.getParameter("usuario");
        if (usuario == null || !USUARIO_VALIDO.matcher(usuario).matches()) {
            response.sendRedirect("cadastro.html?erro=usuario_invalido");
            return;
        }

        // Antes o servidor aceitava senha vazia: o "required" do HTML só vale para quem usa o formulário.
        String senha = request.getParameter("senha");
        if (senha == null || senha.length() < SENHA_MINIMA || senha.length() > SENHA_MAXIMA) {
            response.sendRedirect("cadastro.html?erro=senha_invalida");
            return;
        }

        String dtaNascimento = request.getParameter("dtaNascimento");
        try {
            LocalDate nascimento = LocalDate.parse(dtaNascimento == null ? "" : dtaNascimento);
            if (nascimento.isBefore(NASCIMENTO_MINIMO) || nascimento.isAfter(LocalDate.now(ValidadorItem.FUSO))) {
                response.sendRedirect("cadastro.html?erro=nascimento_invalido");
                return;
            }
        } catch (DateTimeParseException e) {
            response.sendRedirect("cadastro.html?erro=nascimento_invalido");
            return;
        }

        UserModel user = new UserModel();
        user.setUsername(usuario);
        user.setSenha(senha);
        user.setFuncao(funcao);
        user.setDtaNascimento(dtaNascimento);

        // limites = tamanho das colunas em db/init.sql
        try {
            user.setNome(texto(request, "nameFirst", 50));
            user.setSobrenome(texto(request, "sobreNome", 50));
            user.setMatricula(texto(request, "matricula", 50));
            user.setCpf(texto(request, "cpf", 20));
            user.setSexo(texto(request, "opcao", 50));
            user.setEmail(texto(request, "email", 50));
            user.setTelefone(texto(request, "telefone", 25));
            user.setCep(texto(request, "cep", 50));
            user.setEndereco(texto(request, "endereco", 50));
            user.setNumero(texto(request, "numero", 20));
            user.setComplemento(texto(request, "complemento", 50));
            user.setBairro(texto(request, "bairro", 50));
            user.setCidade(texto(request, "cidade", 50));
            user.setEstado(texto(request, "estado", 20));
        } catch (IllegalArgumentException e) {
            response.sendRedirect("cadastro.html?erro=texto_longo");
            return;
        }

        try {
            UserDAO dao = new UserDAO();

            // Antes dava para cadastrar dois usuários com o mesmo nome; no login só o primeiro entrava.
            if (dao.existeUsername(usuario)) {
                response.sendRedirect("cadastro.html?erro=usuario_existente");
                return;
            }

            dao.cadastrar(user);
            response.sendRedirect("dashboard.html?sucesso=funcionario_cadastrado");
        } catch (SQLIntegrityConstraintViolationException e) {
            // Dois cadastros do mesmo nome no mesmo instante: o UNIQUE do banco barra o segundo.
            response.sendRedirect("cadastro.html?erro=usuario_existente");
        } catch (SQLException e) {
            log("Falha ao cadastrar o funcionário", e);
            response.sendRedirect("cadastro.html?erro=erro_servidor");
        }
    }

    /** Lê um campo de texto opcional. Lança IllegalArgumentException se não couber na coluna do banco. */
    private String texto(HttpServletRequest request, String parametro, int tamanhoMaximo) {
        String valor = request.getParameter(parametro);
        if (valor == null || valor.isBlank()) {
            return null;
        }
        valor = valor.trim();
        if (valor.length() > tamanhoMaximo) {
            throw new IllegalArgumentException(parametro);
        }
        return valor;
    }
}
