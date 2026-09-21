package util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.regex.Pattern;
import model.CadastroItensModel;

/**
 * Regras de validação de um item, em um lugar só.
 *
 * Antes as mesmas regras estavam copiadas no CadastroItensController (POST) e no
 * GerenciamentoController (PUT). Regra copiada em dois lugares um dia muda em um
 * e não no outro. Agora os dois controllers chamam validar().
 *
 * validar() devolve null quando está tudo certo, ou o CÓDIGO do erro. O texto
 * que o usuário lê fica no mensagens.js (um único dicionário de mensagens).
 */
public class ValidadorItem {

    /** "Hoje" é o hoje de Salvador, não o do servidor (que em nuvem costuma estar em UTC). */
    public static final ZoneId FUSO = ZoneId.of("America/Bahia");

    private static final LocalDate FABRICACAO_MINIMA = LocalDate.of(2000, 1, 1);
    private static final int ANO_MAXIMO = 9999;                                     // limite do tipo DATE no MySQL
    private static final BigDecimal TOTAL_MAXIMO = new BigDecimal("99999999.99");  // limite do DECIMAL(10,2)
    private static final int TEXTO_MAXIMO = 100;                                    // VARCHAR(100)

    // Os mesmos padrões do atributo pattern do HTML. O HTML é conforto para o
    // usuário; quem protege de verdade é esta validação aqui, no servidor.
    private static final Pattern SO_DIGITOS = Pattern.compile("[0-9]+");
    private static final Pattern TEXTO_NOME = Pattern.compile("[A-Za-zÀ-ÿ0-9 \\-/]+");
    private static final Pattern TEXTO_SIMPLES = Pattern.compile("[A-Za-zÀ-ÿ0-9 ]+");
    private static final Pattern DECIMAL = Pattern.compile("[0-9]+([.,][0-9]{1,2})?");

    private static final Set<String> STATUS_VALIDOS = Set.of("entrada", "saida");
    private static final Set<String> LOCAIS_VALIDOS = Set.of("Freezer", "Refrigerador", "Prateleira");
    private static final Set<String> CATEGORIAS_VALIDAS = Set.of("Matéria Prima", "Produto Final", "Embalagens");
    private static final String CATEGORIA_SEM_VENCIMENTO = "Embalagens";

    private ValidadorItem() {
    }

    /** Converte "5,50" ou "5.50" em BigDecimal. Devolve null se o texto não for um valor válido. */
    public static BigDecimal paraDecimal(String texto) {
        if (texto == null || !DECIMAL.matcher(texto.trim()).matches()) {
            return null;
        }
        return new BigDecimal(texto.trim().replace(",", "."));
    }

    /** Converte texto em Long. Devolve null se não for um inteiro válido. */
    public static Long paraLong(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            return Long.valueOf(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Valida e normaliza o item (tira espaços, zera o vencimento de embalagem,
     * calcula o total). O total NUNCA vem do navegador: quem faz a conta é o servidor.
     */
    public static String validar(CadastroItensModel item, boolean exigirCodigoBarras) {
        item.setNomeItem(limpar(item.getNomeItem()));
        item.setFabricante(limpar(item.getFabricante()));
        item.setMarca(limpar(item.getMarca()));

        if (item.getNomeItem() == null) {
            return "campos_obrigatorios";
        }
        if (!textoValido(item.getNomeItem(), TEXTO_NOME)
                || !textoValido(item.getFabricante(), TEXTO_SIMPLES)
                || !textoValido(item.getMarca(), TEXTO_SIMPLES)) {
            return "texto_invalido";
        }

        if (exigirCodigoBarras) {
            item.setCodigoBarras(limpar(item.getCodigoBarras()));
            if (item.getCodigoBarras() == null) {
                return "campos_obrigatorios";
            }
            if (!textoValido(item.getCodigoBarras(), SO_DIGITOS)) {
                return "texto_invalido";
            }
        }

        if (!STATUS_VALIDOS.contains(String.valueOf(item.getStatus()))
                || !LOCAIS_VALIDOS.contains(String.valueOf(item.getLocal()))
                || !CATEGORIAS_VALIDAS.contains(String.valueOf(item.getCategoria()))) {
            return "opcao_invalida";
        }

        if (item.getQuantidade() == null || item.getQuantidade() < 0
                || item.getEstoqueMinimo() == null || item.getEstoqueMinimo() < 0
                || item.getValor() == null) {
            return "numero_invalido";
        }
        if (item.getValor().signum() <= 0) {
            return "valor_invalido";
        }
        if (item.getValor().stripTrailingZeros().scale() > 2) {
            return "numero_invalido"; // mais de duas casas decimais
        }

        BigDecimal valor = item.getValor().setScale(2, RoundingMode.UNNECESSARY);
        BigDecimal total = valor.multiply(BigDecimal.valueOf(item.getQuantidade()));
        if (valor.compareTo(TOTAL_MAXIMO) > 0 || total.compareTo(TOTAL_MAXIMO) > 0) {
            return "numero_invalido"; // não cabe na coluna DECIMAL(10,2)
        }
        item.setValor(valor);
        item.setTotal(total);

        return validarDatas(item);
    }

    private static String validarDatas(CadastroItensModel item) {
        boolean semVencimento = CATEGORIA_SEM_VENCIMENTO.equals(item.getCategoria());
        boolean temFabricacao = item.getDataFabricacao() != null && !item.getDataFabricacao().isBlank();
        boolean temVencimento = item.getDataVencimento() != null && !item.getDataVencimento().isBlank();

        if (semVencimento) {
            item.setDataVencimento(null);
            if (!temFabricacao) {
                item.setDataFabricacao(null);
                return null; // embalagem: a data de fabricação é opcional
            }
        } else if (!temFabricacao || !temVencimento) {
            return "data_invalida";
        }

        try {
            LocalDate fabricacao = LocalDate.parse(item.getDataFabricacao());

            if (fabricacao.isAfter(LocalDate.now(FUSO))) {
                return "data_futura";
            }
            if (fabricacao.isBefore(FABRICACAO_MINIMA)) {
                return "data_fabricacao_antiga";
            }

            if (!semVencimento) {
                LocalDate vencimento = LocalDate.parse(item.getDataVencimento());
                if (fabricacao.isAfter(vencimento) || vencimento.getYear() > ANO_MAXIMO) {
                    return "data_invalida";
                }
            }
        } catch (DateTimeParseException e) {
            return "data_invalida";
        }

        return null;
    }

    /** Tira espaços das pontas; texto vazio vira null. */
    private static String limpar(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    /** Campo opcional: null é aceito. Se veio, tem que caber na coluna e bater com o padrão. */
    private static boolean textoValido(String texto, Pattern padrao) {
        return texto == null || (texto.length() <= TEXTO_MAXIMO && padrao.matcher(texto).matches());
    }
}
