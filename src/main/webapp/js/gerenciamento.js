let codigoBarrasAtual = null;
let produtosAtuais = [];

const BASE_URL = "/api/estoque";

document.addEventListener("DOMContentLoaded", () => {

    carregarProdutos();

    document.getElementById("modal-quantidade").addEventListener("input", calcularTotal);
    document.getElementById("modal-valor").addEventListener("input", calcularTotal);

    document.getElementById("buscarGerenciamento").addEventListener("input", (e) => {
        carregarProdutos(e.target.value);
    });
});

async function carregarProdutos(busca = "") {

    const container = document.getElementById("listaProdutos");
    container.innerHTML = '<div class="loading-msg">Carregando produtos...</div>';

    try {
        let url = BASE_URL;
        if (busca) {
            url += `?nome=${encodeURIComponent(busca)}`;
        }

        const response = await fetch(url);
        if (response.ok) {
            produtosAtuais = await response.json();
            redenrizarProdutos(produtosAtuais);
        } else {
            container.innerHTML = '<p class="error-msg">Erro ao carregar os produtos.</p>';
        }
    } catch (erro) {
        console.error("Erro na requisição: ", erro);
        container.innerHTML = '<p class="erro-msg">Falha de ligação ao servidor.</p>';
    }
}

    function renderizarProdutos(lista) {
        const container = document.getElementById("listarProdutos");
        container.innerHTML = "";

        if (lista.length === 0) {
            container.innerHTML = '<p class="sem-resultado">Nenhum produto encontrado.</p>';
            return;
        }

        lista.forEach(produto => {
            const card = document.createElement("div");
            card.className = "card-produto";

            const badgeClass = produto.status === "entrada" ? "badge-entrada" : "badge-saida";
            
            let alertaCompraHtml = "";
            let estiloEstoqueBaixo = "";
            
            if(produto.quantidade <=5){
                estiloEstoquebaixo = 
                
            }

            card.innerHTML = `
        <div class="card-nome">${produto.nomeProduto}</div>
        <div class="card-info">Marca: <span>${produto.marca || '-'}</span></div>
        <div class="card-info">Qtd: <span>${produto.quantidade}</span></div>
        <div class="card-info">Valor: Unit.: <span>R$ ${parseFloat(produto.valor).toFixed(2)}</span></div>
        <span class="badge-status ${badgeClass}">${produto.status}</span>
        <button class="btn-gerenciar" onclick="abrirModal('${produto.codigoBarras}')">Editar</button>
        `
            container.appendChild(card);
        });
    }

    function calcularTotal() {

        const qtd = parseFloat(document.getElementById("modal-quantidade").value) || 0;
        const valor = parseFloat(document.getElementById("modal-valor").value) || 0;
        document.getElementById("modal-total").value = (qtd * valor).toFixed(2);
}