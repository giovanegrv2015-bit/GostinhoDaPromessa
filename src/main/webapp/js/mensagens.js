const MENSAGENS = {

    //aq é erro de login
    "login_invalido": "Usuário ou senha incorretos. Tente novamente.",

    //aq é aviso de permissao
    "negado": "Você não tem permissão para acessar essa área.",

    //cadastro de func
    "funcao_invalida": "Função inválida. Selecione uma opção da lista.",
    "nascimento_invalido": "Data de nascimento inválida (deve ser entre 1950 e hoje).",

    //cadastro de item
    "numero_invalido": "Preencha corretamente os campos numéricos (quantidade, valor e estoque mínimo).",
    "valor_invalido": "O valor unitário deve ser maior que zero",
    "data_invalida": "A data de fabricação não pode ser posterior à data de vencimento.",
    "data_futura": "A data de fabricação não pode ser no futuro.",
    "data_fabricacao_antiga": "A data de fabricação não pode ser anterior a 2000.",
};

function exibirMensagem(){
    const params = new URLSearchParams(window.location.search);
    const codigoErro = params.get("erro");
    const codigoAcesso = params.get("acesso");

    let texto = null;

    if (codigoAcesso === "negado") {
        texto = MENSAGENS["negado"];
    }else if (codigoErro && MENSAGENS[codigoErro]) {
        texto = MENSAGENS[codigoErro];
    }

    if (!texto) return;

    const banner = document.createElement("div");
    banner.className = "banner-mensagem";
    banner.textContent = texto;
    document.body.prepend(banner);

    params.delete("erro");
    params.delete("acesso");
    const novaUrl = window.location.pathname +
    (params.toString() ? "?" + params.toString() : "");
    window.history.replaceState({}, "", novaUrl);
}

document.addEventListener("DOMContentLoaded", exibirMensagem);