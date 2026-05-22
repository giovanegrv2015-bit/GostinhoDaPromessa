let codigoBarrasAtual = null;
let todosProudutos = [];

const BASE_URL = "/api/estoque";

document.addEventListener("DOMContentLoaded", () => {
    carregarProdutos();

    document.getElementById("modal-quantidade").addEventListener("input", calcularTottal);
    
});
