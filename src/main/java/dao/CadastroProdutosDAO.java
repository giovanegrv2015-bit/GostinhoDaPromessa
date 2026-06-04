package dao;

import connection.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.CadastroProdutoModel;

public class CadastroProdutosDAO {
    public boolean salvar(CadastroProdutoModel produto){
        String sql = "INSERT INTO produtos "+
                     "(codigo_barras, nome_produto, fabricante, marca, data_fabricacao, data_vencimento, quantidade, valor, total, status) "+
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, produto.getCodigoBarras());
            stmt.setString(2, produto.getNomeProduto());
            stmt.setString(3, produto.getFabricante());
            stmt.setString(4, produto.getMarca());
            stmt.setDate(5, java.sql.Date.valueOf(produto.getDataFabricacao()));
            stmt.setDate(6, java.sql.Date.valueOf(produto.getDataVencimento()));
            stmt.setLong(7, produto.getQuantidade());
            stmt.setString(8, produto.getValor());
            stmt.setString(9, produto.getTotal());
            stmt.setString(10, produto.getStatus());
            
            stmt.executeUpdate();
            
            return true;
        }catch (SQLException e){
        e.printStackTrace();
        return false;
        }           
    }
    
    public List<CadastroProdutoModel> listarComFiltro(String nome, String tipo, String data) {
        List<CadastroProdutoModel> lista = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder( "SELECT * FROM produtos WHERE 1=1");

        if(nome != null && !nome.isEmpty()) {
            sql.append(" AND LOWER(nome_produto) LIKE ?");
        }
        if(tipo != null && !tipo.isEmpty()) {
            sql.append(" AND status = ?");
        }
        if(data != null && !data.isEmpty()) {
            sql.append(" AND data_vencimento = ?");
        }

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql.toString());){
            
            int index = 1;
            
            if(nome != null && !nome.isEmpty()) {
                stmt.setString(index++, "%" + nome.toLowerCase() + "%");
            }
            if(tipo != null && !tipo.isEmpty()) {
                stmt.setString(index++, tipo);
            }
            if(data != null && !data.isEmpty()) {
                stmt.setString(index++, data);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()){
                CadastroProdutoModel p = new CadastroProdutoModel();
                
                p.setCodigoBarras(rs.getString("codigo_barras"));
                p.setNomeProduto(rs.getString("nome_produto"));
                p.setFabricante(rs.getString("fabricante"));
                p.setMarca(rs.getString("marca"));
                p.setDataFabricacao(rs.getDate("data_fabricacao").toLocalDate().toString());
                p.setDataVencimento(rs.getDate("data_vencimento").toLocalDate().toString());
                p.setQuantidade(rs.getLong("quantidade"));
                p.setValor(rs.getString("valor"));
                p.setTotal(rs.getString("total"));
                p.setStatus(rs.getString("status"));
                p.setEstoque(rs.getString("estoque"));
                
                lista.add(p);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
    
    public boolean atualizar(CadastroProdutoModel produto) {
        String sql = "UPDATE produtos SET " +
                    "nome_produto = ?, fabricante = ?, marca " +
                    "data_fabricacao = ?, data_vencimento = ?, " +
                    "quantidade = ?, valor = ?, total = ?, status = ?, estoque = ? " +
                    "WHERE codigo_barras = ? ";
        
                    try (Connection conn = ConnectionFactory.getConnection();
                            PreparedStatement stmt = conn.prepareStatement(sql)) {
                        
                        stmt.setString(1, produto.getNomeProduto());
                        stmt.setString(2, produto.getFabricante());
                        stmt.setString(3, produto.getMarca());
                        stmt.setDate(4, java.sql.Date.valueOf(produto.getDataFabricacao()));
                        stmt.setDate(5, java.sql.Date.valueOf(produto.getDataVencimento()));
                        stmt.setLong(6, produto.getQuantidade());
                        stmt.setString(7, produto.getValor());
                        stmt.setString(8, produto.getTotal());
                        stmt.setString(9, produto.getStatus());
                        stmt.setString(10, produto.getEstoque());
                        
                        stmt.setString(1, produto.getCodigoBarras());
                        
                        int linhasAfetadas = stmt.executeUpdate();
                        return linhasAfetadas > 0;
                        
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return false;
                    }
    }
    
    public boolean excluir(String codigoBarras) {
        String sql = "DELETE FROM produtos WHERE codigo_barras = ?";
        
        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, codigoBarras);
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
        }catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}