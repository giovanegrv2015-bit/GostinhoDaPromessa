package util;

import dao.UserDAO;
import java.sql.SQLException;
import java.util.logging.Logger;
import model.UserModel;

/**
 * Cria os primeiros usuários a partir de variáveis de ambiente (arquivo .env).
 *
 * Por quê: antes o init.sql criava admin/admin1234 e três usuários demo com
 * senha conhecida, e esse arquivo está público no GitHub. Em produção isso é
 * deixar a chave na porta. Agora o init.sql só cria as tabelas, e:
 *
 *   ADMIN_USER + ADMIN_PASSWORD ... cria esse ADMIN se ele ainda não existir
 *   SEED_DEMO=true ................ cria gerente / funcionario / visitante (senha demo1234)
 *                                   -> só para demonstração local, NUNCA em produção
 *
 * Roda no primeiro login (e não na subida do Tomcat) porque nessa hora o banco
 * com certeza já está no ar; na subida ele ainda pode estar inicializando.
 * Trocar ADMIN_PASSWORD no .env depois NÃO altera a senha de um usuário que já existe.
 */
public class UsuariosIniciais {

    private static final Logger LOG = Logger.getLogger(UsuariosIniciais.class.getName());
    private static final int SENHA_MINIMA = 8;
    private static final int SENHA_MAXIMA = 72;    // limite do BCrypt
    private static final int USUARIO_MAXIMO = 100; // VARCHAR(100)
    private static final String SENHA_DEMO = "demo1234";

    private static boolean jaVerificado = false;

    private UsuariosIniciais() {
    }

    public static synchronized void garantir() throws SQLException {
        if (jaVerificado) {
            return;
        }

        UserDAO dao = new UserDAO();

        String adminUser = System.getenv("ADMIN_USER");
        String adminSenha = System.getenv("ADMIN_PASSWORD");

        if (adminUser == null || adminUser.isBlank() || adminSenha == null || adminSenha.isBlank()) {
            LOG.warning("ADMIN_USER/ADMIN_PASSWORD nao definidos no .env: nenhum administrador sera criado automaticamente.");
        } else if (adminSenha.length() < SENHA_MINIMA || adminSenha.length() > SENHA_MAXIMA
                || adminUser.trim().length() > USUARIO_MAXIMO) {
            LOG.severe("ADMIN_USER (ate " + USUARIO_MAXIMO + " caracteres) ou ADMIN_PASSWORD (de " + SENHA_MINIMA
                    + " a " + SENHA_MAXIMA + " caracteres) fora do limite: administrador NAO foi criado.");
        } else {
            criarSeNaoExistir(dao, adminUser.trim(), adminSenha, "ADMIN", "Administrador");
        }

        if ("true".equalsIgnoreCase(System.getenv("SEED_DEMO"))) {
            LOG.warning("SEED_DEMO=true: criando usuarios de demonstracao com senha publica. Nao use em producao.");
            criarSeNaoExistir(dao, "gerente", SENHA_DEMO, "GERENTE", "Gerente");
            criarSeNaoExistir(dao, "funcionario", SENHA_DEMO, "FUNCIONARIO", "Funcionario");
            criarSeNaoExistir(dao, "visitante", SENHA_DEMO, "VISITANTE", "Visitante");
        }

        // só marca como feito se chegou até aqui sem exceção; se o banco falhou, tenta de novo no próximo login
        jaVerificado = true;
    }

    private static void criarSeNaoExistir(UserDAO dao, String username, String senha, String funcao, String nome)
            throws SQLException {
        if (dao.existeUsername(username)) {
            return;
        }
        UserModel user = new UserModel();
        user.setUsername(username);
        user.setSenha(senha);
        user.setFuncao(funcao);
        user.setNome(nome);
        dao.cadastrar(user);
        LOG.info("Usuario inicial criado: " + username + " (" + funcao + ")");
    }
}
