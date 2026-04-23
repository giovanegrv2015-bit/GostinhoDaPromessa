package dao;

import model.CadastroUsuarioModel;
import connection.ConnectionFactory;
import java.sql.PreparedStatement;
import util.SenhaUtil;


public class CadastroUsersDAO {
    
    public boolean cadastrar(CadastroUsuarioModel user) {
        String sql = "INSERT INTO users " +
                    "(username, psw, nameFirst, sobreNome, matricula, cpf, sexo, dtaNascimento, email ,telefone, funcao, cep, endereco, numero, complemento, bairro, cidade, estado)"+
                     "VALUE (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (var con = connection.ConnectionFactory.getConnection()) {
            
            PreparedStatement stmt = con.prepareStatement(sql);
            
            String senhaHash = SenhaUtil.gerarHash(user.getSenha());
            
            stmt.setString(1, user.getNomeUsuario());
            stmt.setString(2, senhaHash);
            stmt.setString(3, user.getNome());
            stmt.setString(4, user.getSobrenome());
            stmt.setString(5, user.getMatricula());
            stmt.setString(6, user.getCpf());
            stmt.setString(7, user.getSexo());
            stmt.setString(8, user.getDtaNascimento());
            stmt.setString(9, user.getEmail());
            stmt.setString(10, user.getTelefone());
            stmt.setString(11, user.getFuncao());
            stmt.setString(12, user.getCep());
            stmt.setString(13, user.getEndereco());
            stmt.setLong(14, user.getNumero());
            stmt.setString(15, user.getComplemento());
            stmt.setString(16, user.getBairro());
            stmt.setString(17, user.getCidade());
            stmt.setString(18, user.getEstado());
            
            stmt.executeUpdate();
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}



