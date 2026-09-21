package util;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Um único lugar para escrever respostas JSON.
 *
 * Antes cada controller repetia setContentType + setCharacterEncoding + write,
 * e alguns montavam o JSON concatenando String (que quebra se o texto tiver aspas).
 * O Gson cuida do escape.
 */
public class JsonUtil {

    public static final Gson GSON = new Gson();

    private JsonUtil() {
    }

    public static void enviar(HttpServletResponse response, int status, Object corpo) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(GSON.toJson(corpo));
    }

    /** Resposta de erro no formato {"erro":"codigo"}. O texto da mensagem fica no mensagens.js. */
    public static void enviarErro(HttpServletResponse response, int status, String codigo) throws IOException {
        enviar(response, status, Map.of("erro", codigo));
    }
}
