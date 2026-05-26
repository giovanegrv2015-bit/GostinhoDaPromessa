let codigoBarrasAtual = null;
let todosProudutos = [];

const BASE_URL = "/api/estoque";

document.addEventListener("DOMContentLoaded", () => {
    carregarProdutos();

    document.getElementById("modal-quantidade").addEventListener("input", calcularTottal);
    document.getElementById("modal-valor").addEventListener("input", calcularTotal);

    document.getElementById("buscarGerenciamento").addEventListener("input", (e) => {
});
});

async function carregarProdutos(busca = "") {
    const container = document.getElementById("listarProdutos");
    container.innerHTML = '<div class="loading-msg">Carregando produtos...</div';

    try {
        let url = BASE_URL;
        if(busca) {
            url += `?nome=${encodeURIComponent(busca)}`;
        }

        const response = await fetch(url);
        if(response.ok) {
            produtosAtuais = await response.json();
        } else {
            container.innerHTML = '<p class="error-msg">Falha de conexão com o servidor.</p>';
        }
}
