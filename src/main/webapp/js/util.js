// Funções usadas por mais de uma página. Carregue este arquivo ANTES dos outros scripts.

// Troca os caracteres que o navegador interpretaria como HTML.
// Sem isso, um item chamado <img src=x onerror=...> vira código rodando na tela
// de quem abrir a lista (XSS). Todo texto que veio do banco passa por aqui antes do innerHTML.
function escaparHtml(valor) {
    if (valor === null || valor === undefined) return "";
    return String(valor)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

// "2026-09-17" -> "17/09/2026". Sem data (embalagens) vira "-" em vez de "undefined".
function formatarData(dataIso) {
    if (!dataIso) return "-";
    const [ano, mes, dia] = dataIso.split("-");
    return `${dia}/${mes}/${ano}`;
}

// 5.5 -> "R$ 5,50"
function formatarMoeda(valor) {
    const numero = Number(valor);
    if (valor === null || valor === undefined || Number.isNaN(numero)) return "-";
    return numero.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

// Data de hoje no fuso do usuário, no formato do <input type="date">.
// new Date().toISOString() devolve a data em UTC: em Salvador, depois das 21h, já é "amanhã".
function hojeLocalISO() {
    const agora = new Date();
    const mes = String(agora.getMonth() + 1).padStart(2, "0");
    const dia = String(agora.getDate()).padStart(2, "0");
    return `${agora.getFullYear()}-${mes}-${dia}`;
}

// fetch + tratamento padrão de erro. Devolve o JSON da resposta.
// Se a resposta não for OK, lança um Error com .status e .codigo (o código que o servidor mandou em {"erro": "..."}).
async function buscarJson(url, opcoes) {
    const response = await fetch(url, opcoes);

    if (response.status === 401) {
        // sessão expirou: volta para o login em vez de deixar a tela vazia sem explicação
        window.location.href = "../index.html?erro=sessao_expirada";
        // A página já está saindo. Uma Promise que nunca termina faz quem chamou parar aqui,
        // sem cair no catch e piscar uma mensagem de erro falsa na tela.
        return new Promise(() => {});
    }

    const corpo = await response.json().catch(() => null);

    if (!response.ok) {
        const erro = new Error("Requisição falhou: " + response.status);
        erro.status = response.status;
        erro.codigo = corpo && corpo.erro ? corpo.erro : "erro_servidor";
        throw erro;
    }

    return corpo;
}
