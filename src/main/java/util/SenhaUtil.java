package util;

import org.mindrot.jbcrypt.BCrypt;

public class SenhaUtil {

    private SenhaUtil() {
    }

    public static String gerarHash(String senha) {
        return BCrypt.hashpw(senha, BCrypt.gensalt());
    }

    public static boolean verificarSenha(String senhaDigitada, String hash) {
        if (senhaDigitada == null || hash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(senhaDigitada, hash);
        } catch (IllegalArgumentException e) {
            // hash corrompido ou fora do formato BCrypt no banco: trata como senha errada
            return false;
        }
    }
}
