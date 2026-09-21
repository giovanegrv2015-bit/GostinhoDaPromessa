package dao;

import connection.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import model.UserModel;
import util.SenhaUtil;

/**
 * Acesso à tabela users: login e cadastro.
 * Antes eram dois DAOs (UserDAO e CadastroUsersDAO) para a mesma tabela.
 */
public class UserDAO {

    /** Devolve o usuário se a senha conferir; null se usuário ou senha estiverem errados. */
    public UserModel validarLogin(String username, String senha) throws SQLException {
        if (username == null || username.isBlank() || senha == null || senha.isEmpty()) {
            return null;
        }

        String sql = "SELECT id, username, psw, funcao FROM users WHERE username = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                if (!SenhaUtil.verificarSenha(senha, rs.getString("psw"))) {
                    return null;
                }

                // O hash não sai daqui: quem chama só precisa saber quem é e qual o perfil.
                UserModel user = new UserModel();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setFuncao(rs.getString("funcao"));
                return user;
            }
        }
    }

    public boolean existeUsername(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void cadastrar(UserModel user) throws SQLException {
        String sql = "INSERT INTO users "
                + "(username, psw, nameFirst, sobreNome, matricula, cpf, sexo, dtaNascimento, email, telefone, "
                + "funcao, cep, endereco, numero, complemento, bairro, cidade, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, SenhaUtil.gerarHash(user.getSenha()));
            stmt.setString(3, user.getNome());
            stmt.setString(4, user.getSobrenome());
            stmt.setString(5, user.getMatricula());
            stmt.setString(6, user.getCpf());
            stmt.setString(7, user.getSexo());

            if (user.getDtaNascimento() == null || user.getDtaNascimento().isBlank()) {
                stmt.setNull(8, Types.DATE);
            } else {
                stmt.setObject(8, LocalDate.parse(user.getDtaNascimento()));
            }

            stmt.setString(9, user.getEmail());
            stmt.setString(10, user.getTelefone());
            stmt.setString(11, user.getFuncao());
            stmt.setString(12, user.getCep());
            stmt.setString(13, user.getEndereco());
            stmt.setString(14, user.getNumero());
            stmt.setString(15, user.getComplemento());
            stmt.setString(16, user.getBairro());
            stmt.setString(17, user.getCidade());
            stmt.setString(18, user.getEstado());

            stmt.executeUpdate();
        }
    }
}
