package connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    private static final String URL = System.getenv("DB_URL");
    private static final String USER = System.getenv("DB_USER");
    private static final String PASSWORD = System.getenv("DB_PASS");
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    private ConnectionFactory() {
    }

    /**
     * Abre uma conexão nova com o banco.
     *
     * Antes este método devolvia null quando algo dava errado, e quem chamava
     * estourava NullPointerException longe da causa real. Agora o erro sobe como
     * SQLException, com a causa verdadeira, e quem chama decide o que responder.
     */
    public static Connection getConnection() throws SQLException {
        if (URL == null || USER == null || PASSWORD == null) {
            throw new SQLException("Variáveis de ambiente DB_URL, DB_USER e DB_PASS não configuradas.");
        }

        try {
            // Com o driver dentro de WEB-INF/lib o Tomcat não garante o registro
            // automático dele, então o Class.forName continua necessário.
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC do MySQL não encontrado.", e);
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
