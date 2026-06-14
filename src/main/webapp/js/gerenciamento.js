let codigoBarrasAtual = null;
let itensAtuais = [];

const BASE_URL = "/api/estoque";
const BASE_GERENCIAMENTO = "/api/gerenciamento"

document.addEventListener("DOMContentLoaded", () => {
    carregarItens();

    document.getElementById("modal-quantidade").addEventListener("input", calcularTotal);
    document.getElementById("modal-valor").addEventListener("input", calcularTotal);

    document.getElementById("buscarGerenciamento").addEventListener("input", (e) => {
        carregarItens(e.target.value);
    });

    document.getElementById("btnFecharModal").addEventListener("click", fecharModal);
    document.getElementById("btnCancelarModal").addEventListener("click", fecharModal);
    document.getElementById("btnSalvarEdicao").addEventListener("click", salvarAlteracoes);
    document.getElementById("btnExcluirItem").addEventListener("click", () => {
        excluirItem(codigoBarrasAtual);
    });

    document.getElementById("modalOverlay").addEventListener("click", (e) => {
        if (e.target === document.getElementById("modalOverlay")) fecharModal();
    });
});

async function carregarItens(busca = "") {

    const container = document.getElementById("listaItens");
    container.innerHTML = '<div class="loading-msg">Carregando itens...</div>';

    try {
        let url = BASE_URL;
        if (busca) {
            url += `?nome=${encodeURIComponent(busca)}`;
        }

        const response = await fetch(url);
        if (response.ok) {
            itensAtuais = await response.json();
            renderizarItens(itensAtuais);
        } else {
            container.innerHTML = '<p class="error-msg">Erro ao carregar os itens.</p>';
        }
    } catch (erro) {
        console.error("Erro na requisição: ", erro);
        container.innerHTML = '<p class="erro-msg">Falha de ligação ao servidor.</p>';
    }
}

    function renderizarItens(lista) {
        const container = document.getElementById("listaItens");
        container.innerHTML = "";

        if (lista.length === 0) {
            container.innerHTML = '<p class="sem-resultado">Nenhum item encontrado.</p>';
            return;
        }

        lista.forEach(item => {
            const card = document.createElement("div");
            card.className = "card-item";

            const badgeClass = item.status === "entrada" ? "badge-entrada" : "badge-saida";

            const estoqueBaixo = item.estoqueMinimo > 0 && item.quantidade <= item.estoqueMinimo;
            const alertaHtml = estoqueBaixo ? `
                <div class="alerta-reposicao">
                    Estoque baixo - reposicao necessária
                    <button class="btn-nota" onclick="emitirNotaCompra('${item.codigoBarras}')">
                    Emitir Nota de Compra
                    </button>
                </div>` : "";

             card.innerHTML = `
                <div class="card-nome">${item.nomeItem}</div>
                <div class="card-info">Cód. Barras: <span>${item.codigoBarras}</span></div>
                <div class="card-info">Fabricante: <span>${item.fabricante || '-'}</span></div>
                <div class="card-info">Marca: <span>${item.marca || '-'}</span></div>
                <div class="card-info">Local: <span>${item.local || '-'}</span></div>
                <div class="card-info">Categoria: <span>${item.categoria || '-'}</span></div>
                <div class="card-info">Qtd. <span class="${estoqueBaixo ? 'qtd-baixa' : ''}">${item.quantidade}</span></div>
                <div class="card-info">Estoque mínimo: <span>${item.estoqueMinimo}</span></div>
                <div class="card-info">Valor Unit.:<span>R$ ${parseFloat(item.valor).toFixed(2)}</span></div>
                <div class="card-info">Vencimento: <span>${item.dataVencimento}</span></div>
                <span class="badge-status ${badgeClass}">${item.status}</span>
                ${alertaHtml}
                <div class="acoes-card">
                <button class="btn-gerenciar" onclick="abrirModal('${item.codigoBarras}')">Editar</button>
                </div>
                `;
                container.appendChild(card);
    });
}

function abrirModal(codigoBarras) {
    const item = itensAtuais.find(i => i.codigoBarras === codigoBarras);
    if(!item) return;

    codigoBarrasAtual = item.codigoBarras;

    document.getElementById("modalTitulo").textContent = item.nomeItem;
    document.getElementById("modal-nomeItem").value = item.nomeItem;
    document.getElementById("modal-fabricante").value = item.fabricante || '';
    document.getElementById("modal-marca").value = item.marca || '';
    document.getElementById("modal-dataFabricacao").value = item.dataFabricacao || '';
    document.getElementById("modal-dataVencimento").value = item.dataVencimento || '';
    document.getElementById("modal-quantidade").value = item.quantidade || '';
    document.getElementById("modal-valor").value = item.valor || '';
    document.getElementById("modal-total").value = item.total || '';
    document.getElementById("modal-status").value = item.status || '';
    document.getElementById("modal-local").value = item.local || '';
    document.getElementById("modal-categoria").value = item.categoria || '';
    document.getElementById("modal-estoqueMinimo").value = item.estoqueMinimo || 0;

    document.getElementById("modalOverlay").style.display = "flex";
}

function fecharModal() {
    document.getElementById("modalOverlay").style.display = "none";
    codigoBarrasAtual = null;
}

function calcularTotal() {
    const qtd = parseFloat(document.getElementById("modal-quantidade").value) || 0;
    const valor = parseFloat(document.getElementById("modal-valor").value) || 0;
    document.getElementById("modal-total").value = (qtd * valor).toFixed(2);
}

async function salvarAlteracoes() {
    if(!codigoBarrasAtual) return;

    const body = {
        nomeItem: document.getElementById("modal-nomeItem").value,
        fabricante: document.getElementById("modal-fabricante").value,
        marca: document.getElementById("modal-marca").value,
        dataFabricacao: document.getElementById("modal-dataFabricacao").value,
        dataVencimento: document.getElementById("modal-dataVencimento").value,
        quantidade: parseInt(document.getElementById("modal-quantidade").value),
        valor: document.getElementById("modal-valor").value,
        total: document.getElementById("modal-total").value,
        status: document.getElementById("modal-status").value,
        local: document.getElementById("modal-local").value,
        categoria: document.getElementById("modal-categoria").value,
        estoqueMinimo: parseInt(document.getElementById("modal-estoqueMinimo").value) || 0,
    };

    try {
        const response = await fetch (
        `${BASE_GERENCIAMENTO}?codigoBarras=${encodeURIComponent(codigoBarrasAtual)}`,
        {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body),
        }
        );

        if(response.ok) {
        fecharModal();
        carregarItens();
        }else{
        alert("Erro ao salvar as alterações. Tente novamente");
        }
    }catch (erro) {
    console.error("Erro no put:", erro);
    alert("Falha de conexão ao salvar.");
    }
}

function emitirNotaCompra(codigoBarras) {
    const item = itensAtuais.find(i => i.codigoBarras === codigoBarras);
    if(!item) return;

    const necessario = (item.estoqueMinimo * 2) - item.quantidade;
    alert(
        `NOTA DE COMPRA\n\n` +
        `Item: ${item.nomeItem}\n ` +
        `Local: ${item.local || 'Não informado'}\n` +
        `Categoria: ${item.categoria || 'Não informada'}\n` +
        `Estoque atual: ${item.quantidade}\n` +
        `Quantidade sugerida para repor: ${necessario} unidades.`
    );
}