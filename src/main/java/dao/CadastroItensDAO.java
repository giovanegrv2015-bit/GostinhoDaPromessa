package dao;

import connection.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.CadastroItensModel;

//onde cadastra o item
public class CadastroItensDAO {
    public boolean salvar(CadastroItensModel item){
        String sql = "INSERT INTO itens "+
                     "(codigo_barras, nome_item, fabricante, marca, data_fabricacao, data_vencimento, quantidade, valor, total, status, local, categoria, estoque_minimo) "+
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, item.getCodigoBarras());
            stmt.setString(2, item.getNomeItem());
            stmt.setString(3, item.getFabricante());
            stmt.setString(4, item.getMarca());
            stmt.setDate(5, java.sql.Date.valueOf(item.getDataFabricacao()));
            stmt.setDate(6, java.sql.Date.valueOf(item.getDataVencimento()));
            stmt.setLong(7, item.getQuantidade());
            stmt.setString(8, item.getValor());
            stmt.setString(9, item.getTotal());
            stmt.setString(10, item.getStatus());
            stmt.setString(11, item.getLocal());
            stmt.setString(12, item.getCategoria());
            stmt.setLong(13, item.getEstoqueMinimo());

            stmt.executeUpdate();
            
            return true;
        }catch (SQLException e){
        e.printStackTrace();
        return false;
        }           
    }
    
    public List<CadastroItensModel> listarComFiltro(String nome, String tipo, String data) {
        List<CadastroItensModel> lista = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder( "SELECT * FROM itens WHERE 1=1");

        if(nome != null && !nome.isEmpty()) {
            sql.append(" AND LOWER(nome_item) LIKE ?");
        }
        if(tipo != null && !tipo.isEmpty()) {
            sql.append(" AND status = ?");
        }
        if(data != null && !data.isEmpty()) {
            sql.append(" AND data_vencimento = ?");
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
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

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CadastroItensModel i = new CadastroItensModel();

                    i.setCodigoBarras(rs.getString("codigo_barras"));
                    i.setNomeItem(rs.getString("nome_item"));
                    i.setFabricante(rs.getString("fabricante"));
                    i.setMarca(rs.getString("marca"));
                    i.setDataFabricacao(rs.getDate("data_fabricacao").toLocalDate().toString());
                    i.setDataVencimento(rs.getDate("data_vencimento").toLocalDate().toString());
                    i.setQuantidade(rs.getLong("quantidade"));
                    i.setValor(rs.getString("valor"));
                    i.setTotal(rs.getString("total"));
                    i.setStatus(rs.getString("status"));
                    i.setLocal(rs.getString("local"));
                    i.setCategoria(rs.getString("categoria"));
                    i.setEstoqueMinimo(rs.getLong("estoque_minimo"));

                    lista.add(i);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
    
    public boolean atualizar(CadastroItensModel item) {
        String sql = "UPDATE itens SET " +
                    "nome_item = ?, fabricante = ?, marca = ?, " +
                    "data_fabricacao = ?, data_vencimento = ?, " +
                    "quantidade = ?, valor = ?, total = ?, status = ?," +
                    "local = ?, categoria = ?, estoque_minimo = ? " +
                    "WHERE codigo_barras = ? ";
        
                    try (Connection conn = ConnectionFactory.getConnection();
                            PreparedStatement stmt = conn.prepareStatement(sql)) {
                        
                        stmt.setString(1, item.getNomeItem());
                        stmt.setString(2, item.getFabricante());
                        stmt.setString(3, item.getMarca());
                        stmt.setDate(4, java.sql.Date.valueOf(item.getDataFabricacao()));
                        stmt.setDate(5, java.sql.Date.valueOf(item.getDataVencimento()));
                        stmt.setLong(6, item.getQuantidade());
                        stmt.setString(7, item.getValor());
                        stmt.setString(8, item.getTotal());
                        stmt.setString(9, item.getStatus());
                        stmt.setString(10, item.getLocal());
                        stmt.setString(11, item.getCategoria());
                        stmt.setLong(12, item.getEstoqueMinimo());
                        
                        stmt.setString(13, item.getCodigoBarras());
                        
                        int linhasAfetadas = stmt.executeUpdate();
                        return linhasAfetadas > 0;
                        
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return false;
                    }
    }
    
    public boolean excluir(String codigoBarras) {
        String sql = "DELETE FROM itens WHERE codigo_barras = ?";
        
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