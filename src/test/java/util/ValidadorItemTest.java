package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import model.CadastroItensModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Testes de ValidadorItem.validar(). Cada teste monta um item a partir de itemValido()
 * (um item completo e correto) e altera só o campo que quer testar. Assim, se um teste
 * falha, o problema está no campo alterado, não em algum outro dado do item que
 * "por acaso" também estava errado.
 *
 * validar() devolve null quando está tudo certo, ou o CÓDIGO do erro (uma String,
 * ex.: "campos_obrigatorios"). Os testes checam esse código, nunca uma mensagem em
 * português: o texto que o usuário lê vive só no mensagens.js do frontend.
 */
class ValidadorItemTest {

    /** Item com todos os campos preenchidos corretamente, categoria "Matéria Prima". */
    private CadastroItensModel itemValido() {
        CadastroItensModel item = new CadastroItensModel();
        item.setCodigoBarras("7891000100103");
        item.setNomeItem("Chocolate em Pó");
        item.setMarca("Garoto");
        item.setDataFabricacao("2025-01-10");
        item.setDataVencimento("2026-01-10");
        item.setQuantidade(10L);
        item.setValor(new BigDecimal("5.50"));
        item.setStatus("entrada");
        item.setLocal("Prateleira");
        item.setCategoria("Matéria Prima");
        item.setEstoqueMinimo(2L);
        return item;
    }

    @Nested
    @DisplayName("Caso feliz")
    class CasoFeliz {

        @Test
        @DisplayName("item completo e correto passa e calcula o total")
        void itemValidoPassaEValorETotalSaoCalculados() {
            CadastroItensModel item = itemValido();

            String erro = ValidadorItem.validar(item);

            assertNull(erro);
            // valor com 2 casas + escala fixada, e total = valor * quantidade,
            // calculado pelo servidor - nunca confiar no que o navegador manda
            assertEquals(new BigDecimal("5.50"), item.getValor());
            assertEquals(new BigDecimal("55.00"), item.getTotal());
        }

        @Test
        @DisplayName("espaços nas pontas do nome e da marca são removidos")
        void textoENormalizado() {
            CadastroItensModel item = itemValido();
            item.setNomeItem("  Chocolate em Pó  ");
            item.setMarca("  Garoto  ");

            String erro = ValidadorItem.validar(item);

            assertNull(erro);
            assertEquals("Chocolate em Pó", item.getNomeItem());
            assertEquals("Garoto", item.getMarca());
        }
    }

    @Nested
    @DisplayName("Campos obrigatórios e texto")
    class CamposETexto {

        @Test
        @DisplayName("nome do item vazio -> campos_obrigatorios")
        void nomeItemVazio() {
            CadastroItensModel item = itemValido();
            item.setNomeItem("   "); // limpar() transforma texto em branco em null

            assertEquals("campos_obrigatorios", ValidadorItem.validar(item));
        }

        @Test
        @DisplayName("nome do item com símbolo não permitido -> texto_invalido")
        void nomeItemComSimboloInvalido() {
            CadastroItensModel item = itemValido();
            item.setNomeItem("Chocolate 70%!");

            assertEquals("texto_invalido", ValidadorItem.validar(item));
        }

        @Test
        @DisplayName("marca com símbolo não permitido -> texto_invalido")
        void marcaComSimboloInvalido() {
            CadastroItensModel item = itemValido();
            item.setMarca("Garoto/Nestlé"); // TEXTO_SIMPLES não aceita "/", diferente do nome do item

            assertEquals("texto_invalido", ValidadorItem.validar(item));
        }

        @Test
        @DisplayName("código de barras vazio é aceito (campo opcional)")
        void codigoBarrasVazioEAceito() {
            CadastroItensModel item = itemValido();
            item.setCodigoBarras(null); // é assim que o PUT de edição manda: o campo não existe na tela

            assertNull(ValidadorItem.validar(item));
        }

        @Test
        @DisplayName("código de barras em branco vira null, não string vazia")
        void codigoBarrasEmBrancoViraNull() {
            CadastroItensModel item = itemValido();
            item.setCodigoBarras("   "); // é o que o formulário manda quando o usuário não digita nada

            assertNull(ValidadorItem.validar(item));
            assertNull(item.getCodigoBarras()); // o banco grava NULL, não ""
        }

        @Test
        @DisplayName("código de barras opcional, mas se vier tem que ser só dígitos")
        void codigoBarrasComLetra() {
            CadastroItensModel item = itemValido();
            item.setCodigoBarras("789100A100103");

            assertEquals("texto_invalido", ValidadorItem.validar(item));
        }
    }

    @Nested
    @DisplayName("Opções de select (status, local, categoria)")
    class Opcoes {

        @Test
        @DisplayName("status fora da lista -> opcao_invalida")
        void statusInvalido() {
            CadastroItensModel item = itemValido();
            item.setStatus("transferencia");

            assertEquals("opcao_invalida", ValidadorItem.validar(item));
        }

        @Test
        @DisplayName("local fora da lista -> opcao_invalida")
        void localInvalido() {
            CadastroItensModel item = itemValido();
            item.setLocal("Armário");

            assertEquals("opcao_invalida", ValidadorItem.validar(item));
        }

        @Test
        @DisplayName("categoria fora da lista -> opcao_invalida")
        void categoriaInvalida() {
            CadastroItensModel item = itemValido();
            item.setCategoria("Bebida");

            assertEquals("opcao_invalida", ValidadorItem.validar(item));
        }
    }

    @Nested
    @DisplayName("Números (quantidade, estoque mínimo, valor)")
    class Numeros {

        @Test
        @DisplayName("quantidade nula -> numero_invalido")
        void quantidadeNula() {
            CadastroItensModel item = itemValido();
            item.setQuantidade(null);

            assertEquals("numero_invalido", ValidadorItem.validar(item));
        }

        @Test
        @DisplayName("quantidade negativa -> numero_invalido")
        void quantidadeNegativa() {
            CadastroItensModel item = itemValido();
            item.setQuantidade(-1L);

            assertEquals("numero_invalido", ValidadorItem.validar(item));
        }

        @Test
        @DisplayName("estoque mínimo negativo -> numero_invalido")
        void estoqueMinimoNegativo() {
            CadastroItensModel item = itemValido();
            item.setEstoqueMinimo(-1L);

            assertEquals("numero_invalido", ValidadorItem.validar(item));
        }

        @Test
        @DisplayName("valor zero -> valor_invalido")
        void valorZero() {
            CadastroItensModel item = itemValido();
            item.setValor(BigDecimal.ZERO);

            assertEquals("valor_invalido", ValidadorItem.validar(item));
        }

        @Test
        @DisplayName("valor negativo -> valor_invalido")
        void valorNegativo() {
            CadastroItensModel item = itemValido();
            item.setValor(new BigDecimal("-5.00"));

            assertEquals("valor_invalido", ValidadorItem.validar(item));
        }

        @Test
        @DisplayName("valor com mais de duas casas decimais -> numero_invalido")
        void valorComTresCasasDecimais() {
            CadastroItensModel item = itemValido();
            item.setValor(new BigDecimal("5.501"));

            assertEquals("numero_invalido", ValidadorItem.validar(item));
        }
    }

    @Nested
    @DisplayName("Datas")
    class Datas {

        @Test
        @DisplayName("categoria != Embalagens sem data de vencimento -> data_invalida")
        void semVencimentoForaDeEmbalagens() {
            CadastroItensModel item = itemValido();
            item.setDataVencimento(null);

            assertEquals("data_invalida", ValidadorItem.validar(item));
        }

        @Test
        @DisplayName("data de fabricação no futuro -> data_futura")
        void dataFabricacaoFutura() {
            CadastroItensModel item = itemValido();
            String amanha = LocalDate.now(ValidadorItem.FUSO).plusDays(1).toString();
            item.setDataFabricacao(amanha);

            assertEquals("data_futura", ValidadorItem.validar(item));
        }

        @Test
        @DisplayName("data de fabricação anterior a 2000 -> data_fabricacao_antiga")
        void dataFabricacaoAntesDe2000() {
            CadastroItensModel item = itemValido();
            item.setDataFabricacao("1999-12-31");

            assertEquals("data_fabricacao_antiga", ValidadorItem.validar(item));
        }

        @Test
        @DisplayName("fabricação depois do vencimento -> data_invalida")
        void fabricacaoDepoisDoVencimento() {
            CadastroItensModel item = itemValido();
            item.setDataFabricacao("2026-06-01");
            item.setDataVencimento("2026-01-01");

            assertEquals("data_invalida", ValidadorItem.validar(item));
        }

        @Test
        @DisplayName("categoria Embalagens sem nenhuma data -> passa e zera as duas")
        void embalagemSemDatas() {
            CadastroItensModel item = itemValido();
            item.setCategoria("Embalagens");
            item.setDataFabricacao(null);
            item.setDataVencimento(null);

            String erro = ValidadorItem.validar(item);

            assertNull(erro);
            assertNull(item.getDataFabricacao());
            assertNull(item.getDataVencimento());
        }

        @Test
        @DisplayName("categoria Embalagens com vencimento informado -> vencimento é ignorado (zerado)")
        void embalagemComVencimentoInformadoEZerado() {
            CadastroItensModel item = itemValido();
            item.setCategoria("Embalagens");
            item.setDataFabricacao(null);
            // mesmo mandando um vencimento, embalagem não tem vencimento no banco
            item.setDataVencimento("2026-01-10");

            String erro = ValidadorItem.validar(item);

            assertNull(erro);
            assertNull(item.getDataVencimento());
        }

        @Test
        @DisplayName("categoria Embalagens com fabricação futura ainda é validada -> data_futura")
        void embalagemAindaValidaFabricacaoSeInformada() {
            CadastroItensModel item = itemValido();
            item.setCategoria("Embalagens");
            item.setDataVencimento(null);
            item.setDataFabricacao(LocalDate.now(ValidadorItem.FUSO).plusDays(1).toString());

            assertEquals("data_futura", ValidadorItem.validar(item));
        }
    }
}
