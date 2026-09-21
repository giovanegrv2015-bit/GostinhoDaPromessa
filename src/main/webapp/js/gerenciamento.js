let idAtual = null;
let itensAtuais = [];

const URL_ESTOQUE = "../api/estoque";
const URL_GERENCIAMENTO = "../api/gerenciamento";

document.addEventListener("DOMContentLoaded", () => {
    carregarItens();

    document.getElementById("modal-quantidade").addEventListener("input", calcularTotal);
    document.getElementById("modal-valor").addEventListener("input", calcularTotal);

    // A busca filtra a lista que já está na memória. Antes cada tecla disparava uma
    // requisição ao servidor, e as respostas podiam chegar fora de ordem.
    document.getElementById("buscarGerenciamento").addEventListener("input", renderizarItens);

    document.getElementById("btnFecharModal").addEventListener("click", fecharModal);
    document.getElementById("btnCancelarModal").addEventListener("click", fecharModal);
    document.getElementById("btnSalvarEdicao").addEventListener("click", salvarAlteracoes);
    document.getElementById("btnExcluirItem").addEventListener("click", () => excluirItem(idAtual));

    document.getElementById("modal-categoria").addEventListener("change", alternarCamposDataModal);
    document.getElementById("modal-dataFabricacao").setAttribute("max", hojeLocalISO());
    document.getElementById("formEdicao").addEventListener("submit", (e) => e.preventDefault());

    document.getElementById("modalOverlay").addEventListener("click", (e) => {
        if (e.target === document.getElementById("modalOverlay")) fecharModal();
    });

    // Um único ouvinte na lista resolve os cliques de todos os cards (delegação de evento).
    // Substitui os onclick="..." escritos dentro do HTML gerado, que a política de
    // segurança (CSP) bloqueia e que obrigavam as funções a serem globais.
    document.getElementById("listaItens").addEventListener("click", (e) => {
        const botao = e.target.closest("button[data-acao]");
        if (!botao) return;

        const id = Number(botao.dataset.id);
        if (botao.dataset.acao === "editar") abrirModal(id);
        if (botao.dataset.acao === "nota") emitirNotaCompra(id);
    });
});

function alternarCamposDataModal() {
    const isEmbalagem = document.getElementById("modal-categoria").value === "Embalagens";
    const campoFabricacao = document.getElementById("modal-dataFabricacao");
    const campoVencimento = document.getElementById("modal-dataVencimento");

    campoVencimento.disabled = isEmbalagem;
    campoVencimento.required = !isEmbalagem;
    campoFabricacao.required = !isEmbalagem;

    if (isEmbalagem) {
        campoVencimento.value = "";
    }
}

async function carregarItens() {
    const container = document.getElementById("listaItens");
    container.innerHTML = '<div class="loading-msg">Carregando itens...</div>';

    try {
        itensAtuais = await buscarJson(URL_ESTOQUE);
        renderizarItens();
    } catch (erro) {
        console.error("Erro ao carregar os itens:", erro);
        container.innerHTML = '<p class="erro-msg">Não foi possível carregar os itens.</p>';
    }
}

function renderizarItens() {
    const container = document.getElementById("listaItens");
    const busca = document.getElementById("buscarGerenciamento").value.trim().toLowerCase();

    const lista = busca
        ? itensAtuais.filter(item => (item.nomeItem || "").toLowerCase().includes(busca))
        : itensAtuais;

    if (lista.length === 0) {
        container.innerHTML = '<p class="sem-resultado">Nenhum item encontrado.</p>';
        return;
    }

    container.innerHTML = lista.map(item => {
        const saida = item.status === "saida";
        const estoqueBaixo = item.estoqueMinimo > 0 && item.quantidade <= item.estoqueMinimo;

        const alertaHtml = estoqueBaixo ? `
            <div class="alerta-reposicao">
                Estoque baixo - reposição necessária
                <button type="button" class="btn-nota" data-acao="nota" data-id="${item.id}">Emitir Nota de Compra</button>
            </div>` : "";

        return `
            <div class="card-item">
                <div class="card-nome">${escaparHtml(item.nomeItem)}</div>
                <div class="card-info">Cód. Barras: <span>${escaparHtml(item.codigoBarras)}</span></div>
                <div class="card-info">Fabricante: <span>${escaparHtml(item.fabricante || "-")}</span></div>
                <div class="card-info">Marca: <span>${escaparHtml(item.marca || "-")}</span></div>
                <div class="card-info">Local: <span>${escaparHtml(item.local || "-")}</span></div>
                <div class="card-info">Categoria: <span>${escaparHtml(item.categoria || "-")}</span></div>
                <div class="card-info">Qtd. <span class="${estoqueBaixo ? "qtd-baixa" : ""}">${escaparHtml(item.quantidade)}</span></div>
                <div class="card-info">Estoque mínimo: <span>${escaparHtml(item.estoqueMinimo)}</span></div>
                <div class="card-info">Valor Unit.: <span>${formatarMoeda(item.valor)}</span></div>
                <div class="card-info">Vencimento: <span>${formatarData(item.dataVencimento)}</span></div>
                <span class="badge-status ${saida ? "badge-saida" : "badge-entrada"}">${saida ? "Saída" : "Entrada"}</span>
                ${alertaHtml}
                <div class="acoes-card">
                    <button type="button" class="btn-gerenciar" data-acao="editar" data-id="${item.id}">Editar</button>
                </div>
            </div>`;
    }).join("");
}

// 5.5 -> "5,50" (formato que o campo de valor espera)
function valorParaCampo(valor) {
    const numero = Number(valor);
    if (valor === null || valor === undefined || Number.isNaN(numero)) return "";
    return numero.toFixed(2).replace(".", ",");
}

function abrirModal(id) {
    const item = itensAtuais.find(i => i.id === id);
    if (!item) return;

    idAtual = item.id;

    document.getElementById("modalTitulo").textContent = item.nomeItem;
    document.getElementById("modal-nomeItem").value = item.nomeItem || "";
    document.getElementById("modal-fabricante").value = item.fabricante || "";
    document.getElementById("modal-marca").value = item.marca || "";
    document.getElementById("modal-dataFabricacao").value = item.dataFabricacao || "";
    document.getElementById("modal-dataVencimento").value = item.dataVencimento || "";
    // "??" e não "||": com "||" a quantidade 0 virava campo vazio e o item não salvava
    document.getElementById("modal-quantidade").value = item.quantidade ?? "";
    document.getElementById("modal-valor").value = valorParaCampo(item.valor);
    document.getElementById("modal-status").value = item.status || "";
    document.getElementById("modal-local").value = item.local || "";
    document.getElementById("modal-categoria").value = item.categoria || "";
    document.getElementById("modal-estoqueMinimo").value = item.estoqueMinimo ?? 0;

    calcularTotal();
    alternarCamposDataModal();

    document.getElementById("modalOverlay").hidden = false;
}

function fecharModal() {
    document.getElementById("modalOverlay").hidden = true;
    idAtual = null;
}

function calcularTotal() {
    const qtd = parseInt(document.getElementById("modal-quantidade").value, 10) || 0;
    const valor = parseFloat(document.getElementById("modal-valor").value.replace(",", ".")) || 0;
    document.getElementById("modal-total").value = (qtd * valor).toFixed(2).replace(".", ",");
}

async function salvarAlteracoes() {
    if (!idAtual) return;

    // Os campos do modal agora ficam dentro de um <form>: o próprio navegador confere
    // required, pattern, min e max e aponta o campo errado. Antes esses atributos eram enfeite.
    const formulario = document.getElementById("formEdicao");
    if (!formulario.reportValidity()) return;

    const categoria = document.getElementById("modal-categoria").value;
    const isEmbalagem = categoria === "Embalagens";
    const dataFabricacao = document.getElementById("modal-dataFabricacao").value;
    const dataVencimento = document.getElementById("modal-dataVencimento").value;
    const valor = document.getElementById("modal-valor").value.replace(",", ".");

    if (!(parseFloat(valor) > 0)) {
        mostrarBanner(textoDaMensagem("valor_invalido"));
        return;
    }
    if (!isEmbalagem && dataFabricacao > dataVencimento) {
        mostrarBanner(textoDaMensagem("data_invalida"));
        return;
    }

    // o total não é enviado: quem calcula é o servidor
    const body = {
        nomeItem: document.getElementById("modal-nomeItem").value,
        fabricante: document.getElementById("modal-fabricante").value,
        marca: document.getElementById("modal-marca").value,
        dataFabricacao: dataFabricacao || null,
        dataVencimento: isEmbalagem ? null : dataVencimento,
        quantidade: parseInt(document.getElementById("modal-quantidade").value, 10),
        valor: valor,
        status: document.getElementById("modal-status").value,
        local: document.getElementById("modal-local").value,
        categoria: categoria,
        estoqueMinimo: parseInt(document.getElementById("modal-estoqueMinimo").value, 10),
    };

    try {
        await buscarJson(`${URL_GERENCIAMENTO}?id=${encodeURIComponent(idAtual)}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body),
        });

        fecharModal();
        await carregarItens();
        mostrarBanner(textoDaMensagem("item_atualizado"), "sucesso");
    } catch (erro) {
        console.error("Erro no PUT:", erro);
        fecharModalSeItemSumiu(erro);
        mostrarBanner(textoDaMensagem(erro.codigo));
    }
}

async function excluirItem(id) {
    if (!id) return;
    if (!confirm("Tem certeza que deseja excluir este item?")) return;

    try {
        await buscarJson(`${URL_GERENCIAMENTO}?id=${encodeURIComponent(id)}`, { method: "DELETE" });

        fecharModal();
        await carregarItens();
        mostrarBanner(textoDaMensagem("item_excluido"), "sucesso");
    } catch (erro) {
        console.error("Erro no DELETE:", erro);
        fecharModalSeItemSumiu(erro);
        mostrarBanner(textoDaMensagem(erro.codigo));
    }
}

// Outra pessoa (ou outra aba) pode ter excluído o item enquanto o modal estava aberto.
function fecharModalSeItemSumiu(erro) {
    if (erro.status === 404) {
        fecharModal();
        carregarItens();
    }
}

function emitirNotaCompra(id) {
    const item = itensAtuais.find(i => i.id === id);
    if (!item) return;

    const necessario = (item.estoqueMinimo * 2) - item.quantidade;
    alert(
        `NOTA DE COMPRA\n\n` +
        `Item: ${item.nomeItem}\n` +
        `Local: ${item.local || "Não informado"}\n` +
        `Categoria: ${item.categoria || "Não informada"}\n` +
        `Estoque atual: ${item.quantidade}\n` +
        `Quantidade sugerida para repor: ${necessario} unidades.`
    );
}
