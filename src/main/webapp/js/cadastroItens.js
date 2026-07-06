document.getElementById("valor").addEventListener("input", calcular);
document.getElementById("quantidade").addEventListener("input", calcular);
document.getElementById("categoria").addEventListener("change", alternarCamposData);

const CAMPOS_NUMERICOS = ["quantidade", "valor", "total", "estoqueMinimo"];
CAMPOS_NUMERICOS.forEach((idCampo) => {
    const campo = document.getElementById(idCampo);
    campo.addEventListener("keydown", (e) => {
        if (["e", "E", "+", "-"].includes(e.key)) {
            e.preventDefault();
        }
    });
});


function calcular(){
    let valor = parseFloat(document.getElementById("valor").value) || 0;
    let quantidade = parseInt(document.getElementById("quantidade").value) || 0;
    
    document.getElementById("total").value = (valor * quantidade).toFixed(2);
}

function alternarCamposData() {
    const categoria = document.getElementById("categoria").value;
    const campoFabricacao = document.getElementById("dataFabricacao");
    const campoVencimento = document.getElementById("dataVencimento");

    const isEmbalagem = categoria === "Embalagens";

    campoFabricacao.disabled = isEmbalagem;
    campoVencimento.disabled = isEmbalagem;

    if (isEmbalagem) {
        campoFabricacao.value = "";
        campoVencimento.value = "";
    }
}