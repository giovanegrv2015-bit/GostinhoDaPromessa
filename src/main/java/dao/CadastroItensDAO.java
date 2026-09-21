package dao;

import connection.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.CadastroItensModel;

/**
 * Acesso à tabela itens.
 *
 * Os métodos lançam SQLException em vez de engolir o erro e devolver false ou
 * lista vazia. Assim o controller consegue diferenciar "não achei" (false) de
 * "o banco caiu" (exceção) e responder 404 ou 500 de verdade.
 */
public class CadastroItensDAO {

    private static final String COLUNAS =
            "id, codigo_barras, nome_item, fabricante, marca, data_fabricacao, data_vencimento, "
            + "quantidade, valor, total, status, local, categoria, estoque_minimo";

    public void salvar(CadastroItensModel item) throws SQLException {
        String sql = "INSERT INTO itens "
                + "(codigo_barras, nome_item, fabricante, marca, data_fabricacao, data_vencimento, "
                + "quantidade, valor, total, status, local, categoria, estoque_minimo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getCodigoBarras());
            preencherCamposEditaveis(stmt, 2, item);

            stmt.executeUpdate();
        }
    }

    public List<CadastroItensModel> listarComFiltro(String nome, String tipo, String data) throws SQLException {
        List<CadastroItensModel> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT " + COLUNAS + " FROM itens WHERE 1=1");

        boolean filtrarNome = nome != null && !nome.isBlank();
        boolean filtrarTipo = tipo != null && !tipo.isBlank();
        boolean filtrarData = data != null && !data.isBlank();

        if (filtrarNome) {
            sql.append(" AND LOWER(nome_item) LIKE ?");
        }
        if (filtrarTipo) {
            sql.append(" AND status = ?");
        }
        if (filtrarData) {
            sql.append(" AND data_vencimento = ?");
        }
        // sem ORDER BY o MySQL devolve em qualquer ordem, e a paginação ficaria instável
        sql.append(" ORDER BY nome_item, id");

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;

            if (filtrarNome) {
                stmt.setString(index++, "%" + nome.trim().toLowerCase() + "%");
            }
            if (filtrarTipo) {
                stmt.setString(index++, tipo);
            }
            if (filtrarData) {
                stmt.setObject(index++, LocalDate.parse(data));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Devolve false quando o id não existe. */
    public boolean atualizar(CadastroItensModel item) throws SQLException {
        String sql = "UPDATE itens SET "
                + "nome_item = ?, fabricante = ?, marca = ?, "
                + "data_fabricacao = ?, data_vencimento = ?, "
                + "quantidade = ?, valor = ?, total = ?, status = ?, "
                + "local = ?, categoria = ?, estoque_minimo = ? "
                + "WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int proximo = preencherCamposEditaveis(stmt, 1, item);
            stmt.setInt(proximo, item.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    /** Devolve false quando o id não existe. */
    public boolean excluir(int id) throws SQLException {
        String sql = "DELETE FROM itens WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /** Totais do dashboard. Antes este SQL morava dentro do controller. */
    public Map<String, Long> resumo() throws SQLException {
        String sql = "SELECT "
                + "COALESCE(SUM(CASE WHEN status = 'entrada' THEN quantidade ELSE 0 END), 0) AS entrada, "
                + "COALESCE(SUM(CASE WHEN status = 'saida' THEN quantidade ELSE 0 END), 0) AS saida "
                + "FROM itens";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            rs.next(); // SUM sem GROUP BY sempre devolve exatamente uma linha
            long entrada = rs.getLong("entrada");
            long saida = rs.getLong("saida");

            Map<String, Long> resultado = new LinkedHashMap<>();
            resultado.put("entrada", entrada);
            resultado.put("saida", saida);
            resultado.put("total", entrada - saida);
            return resultado;
        }
    }

    /**
     * Os 12 campos que o INSERT e o UPDATE têm em comum, na mesma ordem.
     * Antes esse bloco estava copiado nos dois métodos.
     * Devolve o índice do próximo parâmetro livre.
     */
    private int preencherCamposEditaveis(PreparedStatement stmt, int inicio, CadastroItensModel item)
            throws SQLException {
        int i = inicio;
        stmt.setString(i++, item.getNomeItem());
        stmt.setString(i++, item.getFabricante());
        stmt.setString(i++, item.getMarca());
        setData(stmt, i++, item.getDataFabricacao());
        setData(stmt, i++, item.getDataVencimento());
        stmt.setLong(i++, item.getQuantidade());
        stmt.setBigDecimal(i++, item.getValor());
        stmt.setBigDecimal(i++, item.getTotal());
        stmt.setString(i++, item.getStatus());
        stmt.setString(i++, item.getLocal());
        stmt.setString(i++, item.getCategoria());
        stmt.setLong(i++, item.getEstoqueMinimo());
        return i;
    }

    private void setData(PreparedStatement stmt, int indice, String dataIso) throws SQLException {
        if (dataIso == null || dataIso.isBlank()) {
            stmt.setNull(indice, Types.DATE);
        } else {
            // LocalDate não carrega fuso horário, então a data não "anda" um dia
            // dependendo do fuso do servidor (risco que existe com java.sql.Date).
            stmt.setObject(indice, LocalDate.parse(dataIso));
        }
    }

    private CadastroItensModel mapear(ResultSet rs) throws SQLException {
        CadastroItensModel i = new CadastroItensModel();

        i.setId(rs.getInt("id"));
        i.setCodigoBarras(rs.getString("codigo_barras"));
        i.setNomeItem(rs.getString("nome_item"));
        i.setFabricante(rs.getString("fabricante"));
        i.setMarca(rs.getString("marca"));

        LocalDate fabricacao = rs.getObject("data_fabricacao", LocalDate.class);
        LocalDate vencimento = rs.getObject("data_vencimento", LocalDate.class);
        i.setDataFabricacao(fabricacao != null ? fabricacao.toString() : null);
        i.setDataVencimento(vencimento != null ? vencimento.toString() : null);

        i.setQuantidade(rs.getLong("quantidade"));
        i.setValor(rs.getBigDecimal("valor"));
        i.setTotal(rs.getBigDecimal("total"));
        i.setStatus(rs.getString("status"));
        i.setLocal(rs.getString("local"));
        i.setCategoria(rs.getString("categoria"));
        i.setEstoqueMinimo(rs.getLong("estoque_minimo"));

        return i;
    }
}
