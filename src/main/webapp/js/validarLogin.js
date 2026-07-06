async function validarLogin() {
    try {
        const res = await fetch("/api/perfil");
        const dado = await res.json();

        console.log("PERFIL FRONT: ", dado.perfil);

        const perfil = (dado.perfil || "").toUpperCase();
        const PERFIS_COM_ACESSO = ["ADMIN", "GERENTE", "FUNCIONARIO"];

        if(!PERFIS_COM_ACESSO.includes(perfil)) {
            document.querySelectorAll(".btn-app").forEach(btn => {
                    btn.style.display = "none";
            });
        }else if (perfil === "FUNCIONARIO") {
            const btnCadastroFuncionario = document.getElementById("btnCadastroFuncionario");
            if (btnCadastroFuncionario) {
                btnCadastroFuncionario.style.display = "none";
            }
        }
    } catch (e) {
        console.error("Erro ao veriricar o perfil.", e);   
    }

}

validarLogin();