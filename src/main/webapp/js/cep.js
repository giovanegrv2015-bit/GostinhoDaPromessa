// Busca de endereço pelo CEP (ViaCEP).
// Antes havia dois blocos DOMContentLoaded registrando ouvintes no mesmo campo;
// e apertar Enter buscava o CEP duas vezes (uma no Enter, outra no blur).

let ultimoCepBuscado = "";

document.addEventListener("DOMContentLoaded", function () {
    const campoCep = document.getElementById("cep");

    // máscara 00000-000 enquanto digita
    campoCep.addEventListener("input", function () {
        let valor = this.value.replace(/\D/g, "").substring(0, 8);

        if (valor.length > 5) {
            valor = valor.substring(0, 5) + "-" + valor.substring(5);
        }

        this.value = valor;
    });

    campoCep.addEventListener("blur", buscarCep);

    campoCep.addEventListener("keydown", function (e) {
        if (e.key === "Enter") {
            e.preventDefault(); // Enter no CEP busca o endereço em vez de enviar o formulário
            buscarCep();
        }
    });
});

async function buscarCep() {
    const campoCep = document.getElementById("cep");
    const cep = campoCep.value.replace(/\D/g, "");

    if (cep.length !== 8) {
        if (cep.length > 0) {
            mostrarBanner("CEP incompleto: são 8 números.");
            campoCep.style.borderColor = "red";
        }
        return;
    }

    if (cep === ultimoCepBuscado) return;
    ultimoCepBuscado = cep;

    campoCep.style.borderColor = "#aaa";
    preencherCampos({ aguardando: true });

    try {
        const response = await fetch(`https://viacep.com.br/ws/${cep}/json/`);
        const dados = await response.json();

        if (dados.erro) {
            mostrarBanner("CEP não encontrado. Confira os números ou preencha o endereço à mão.");
            limparCamposEndereco();
            campoCep.style.borderColor = "red";
            return;
        }

        preencherCampos(dados);
        campoCep.style.borderColor = "green";
    } catch (e) {
        // o cadastro não pode depender de um serviço externo: sem ViaCEP, preenche à mão
        mostrarBanner("Não foi possível consultar o CEP agora. Preencha o endereço à mão.");
        limparCamposEndereco();
        campoCep.style.borderColor = "red";
        ultimoCepBuscado = "";
        console.error("Erro na busca do CEP", e);
    }
}

function preencherCampos(dados) {
    const CAMPOS = { endereco: "logradouro", cidade: "localidade", bairro: "bairro", estado: "uf" };

    for (const [idCampo, chaveViaCep] of Object.entries(CAMPOS)) {
        document.getElementById(idCampo).value = dados.aguardando ? "Buscando CEP..." : (dados[chaveViaCep] || "");
    }

    if (!dados.aguardando && dados.complemento) {
        document.getElementById("complemento").value = dados.complemento;
    }
}

function limparCamposEndereco() {
    ["endereco", "cidade", "bairro", "estado"].forEach((idCampo) => {
        document.getElementById(idCampo).value = "";
    });
}
