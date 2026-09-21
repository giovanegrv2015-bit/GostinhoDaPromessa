document.getElementById("valor").addEventListener("input", calcular);
document.getElementById("quantidade").addEventListener("input", calcular);
document.getElementById("categoria").addEventListener("change", alternarCamposData);

// hojeLocalISO() vem do util.js. Antes era new Date().toISOString(), que devolve a data em UTC:
// em Salvador, depois das 21h, o "máximo" do campo já era o dia seguinte.
document.getElementById("dataFabricacao").setAttribute("max", hojeLocalISO());

// <input type="number"> aceita "e", "+" e "-" (notação científica). Aqui não faz sentido.
bloquearTeclas(["quantidade", "estoqueMinimo"], ["e", "E", "+", "-", ".", ","]); // só inteiros
bloquearTeclas(["valor"], ["e", "E", "+", "-"]);                                 // vírgula e ponto liberados

function bloquearTeclas(idsDosCampos, teclas) {
    idsDosCampos.forEach((idCampo) => {
        document.getElementById(idCampo).addEventListener("keydown", (e) => {
            if (teclas.includes(e.key)) {
                e.preventDefault();
            }
        });
    });
}

// O total na tela é só uma prévia para o usuário; o valor gravado é calculado no servidor.
function calcular() {
    const valor = parseFloat(document.getElementById("valor").value.replace(",", ".")) || 0;
    const quantidade = parseInt(document.getElementById("quantidade").value, 10) || 0;

    document.getElementById("total").value = (valor * quantidade).toFixed(2).replace(".", ",");
}

function alternarCamposData() {
    const isEmbalagem = document.getElementById("categoria").value === "Embalagens";
    const campoFabricacao = document.getElementById("dataFabricacao");
    const campoVencimento = document.getElementById("dataVencimento");

    campoVencimento.disabled = isEmbalagem;
    campoVencimento.required = !isEmbalagem;
    campoFabricacao.required = !isEmbalagem;

    if (isEmbalagem) {
        campoVencimento.value = "";
    }
}
