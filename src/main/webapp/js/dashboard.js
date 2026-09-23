// Dashboard: resumo, tabela, filtro e paginação.
// Antes a tabela era desenhada por dois arquivos (dashboard.js e filtro.js) com o mesmo
// código copiado, e cada um formatava os valores de um jeito. Agora existe UMA função
// que busca (carregarEstoque) e UMA que desenha (renderizarTabela).

const ITENS_POR_PAGINA = 10;
const TOTAL_COLUNAS = 9;

let itens = [];        // resultado da última busca
let paginaAtual = 1;

async function carregarEstoque() {
    const filtros = new URLSearchParams();
    const nome = document.getElementById("pesquisarNome").value.trim();
    const tipo = document.getElementById("tipoMovimentacao").value;
    const data = document.getElementById("filtroData").value;

    if (nome) filtros.set("nome", nome);
    if (tipo) filtros.set("tipo", tipo);
    if (data) filtros.set("data", data);

    const consulta = filtros.toString();

    try {
        itens = await buscarJson("../api/estoque" + (consulta ? "?" + consulta : ""));
        paginaAtual = 1;
        renderizarTabela();
    } catch (erro) {
        console.error("Erro ao carregar os itens.", erro);
        itens = [];
        mostrarLinhaUnica("Não foi possível carregar os itens.");
        atualizarPaginacao();
    }
}

function totalDePaginas() {
    return Math.max(1, Math.ceil(itens.length / ITENS_POR_PAGINA));
}

function renderizarTabela() {
    if (itens.length === 0) {
        mostrarLinhaUnica("Nenhum item encontrado.");
        atualizarPaginacao();
        return;
    }

    const inicio = (paginaAtual - 1) * ITENS_POR_PAGINA;
    const itensDaPagina = itens.slice(inicio, inicio + ITENS_POR_PAGINA);

    // monta tudo em uma string e escreve no DOM uma vez só
    // (innerHTML += dentro do laço refaz a tabela inteira a cada item)
    document.getElementById("corpoTabela").innerHTML = itensDaPagina.map(item => `
        <tr>
            <td>${escaparHtml(item.codigoBarras || "-")}</td>
            <td>${escaparHtml(item.nomeItem)}</td>
            <td>${escaparHtml(item.marca || "-")}</td>
            <td>${formatarData(item.dataFabricacao)}</td>
            <td>${formatarData(item.dataVencimento)}</td>
            <td>${escaparHtml(item.quantidade)}</td>
            <td>${formatarMoeda(item.valor)}</td>
            <td>${formatarMoeda(item.total)}</td>
            <td>${item.status === "saida" ? "Saída" : "Entrada"}</td>
        </tr>`).join("");

    atualizarPaginacao();
}

function mostrarLinhaUnica(texto) {
    document.getElementById("corpoTabela").innerHTML =
        `<tr><td colspan="${TOTAL_COLUNAS}" class="tabela-vazia">${escaparHtml(texto)}</td></tr>`;
}

function atualizarPaginacao() {
    const total = totalDePaginas();
    const seletor = document.getElementById("pagina");

    seletor.innerHTML = "";
    for (let numero = 1; numero <= total; numero++) {
        const opcao = document.createElement("option");
        opcao.value = numero;
        opcao.textContent = numero;
        seletor.appendChild(opcao);
    }
    seletor.value = paginaAtual;

    document.getElementById("btnVoltar").disabled = paginaAtual <= 1;
    document.getElementById("btnProximo").disabled = paginaAtual >= total;
}

function irParaPagina(numero) {
    paginaAtual = Math.min(Math.max(1, numero), totalDePaginas());
    renderizarTabela();
}

async function carregarResumo() {
    try {
        const dados = await buscarJson("../api/resumo");

        document.getElementById("cardEntrada").textContent = dados.entrada;
        document.getElementById("cardSaida").textContent = dados.saida;
        document.getElementById("cardTotal").textContent = dados.total;
    } catch (erro) {
        console.error("Erro na consulta do resumo.", erro);
    }
}

document.addEventListener("DOMContentLoaded", () => {
    carregarEstoque();
    carregarResumo();

    document.getElementById("btnPesquisar").addEventListener("click", carregarEstoque);
    document.getElementById("pesquisarNome").addEventListener("keydown", (e) => {
        if (e.key === "Enter") carregarEstoque();
    });

    document.getElementById("pagina").addEventListener("change", (e) => irParaPagina(Number(e.target.value)));
    document.getElementById("btnVoltar").addEventListener("click", () => irParaPagina(paginaAtual - 1));
    document.getElementById("btnProximo").addEventListener("click", () => irParaPagina(paginaAtual + 1));
});
