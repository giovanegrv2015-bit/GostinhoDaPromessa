// Ajusta o menu ao perfil de quem está logado. Isto é só aparência: esconder um botão
// não protege nada. Quem bloqueia o acesso de verdade é o AuthFilter, no servidor.
async function validarLogin() {
    try {
        const dado = await buscarJson("../api/perfil");

        const perfil = (dado.perfil || "").toUpperCase();
        const PERFIS_COM_ACESSO = ["ADMIN", "GERENTE", "FUNCIONARIO"];

        if (!PERFIS_COM_ACESSO.includes(perfil)) {
            document.querySelectorAll(".btn-app").forEach(btn => {
                btn.style.display = "none";
            });
        } else if (perfil === "FUNCIONARIO") {
            const btnCadastroFuncionario = document.getElementById("btnCadastroFuncionario");
            if (btnCadastroFuncionario) {
                btnCadastroFuncionario.style.display = "none";
            }
        }
    } catch (e) {
        console.error("Erro ao verificar o perfil.", e);
    }
}

validarLogin();
