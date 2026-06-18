async function validarLogin() {
    try {
        const res = await fetch("http://localhost:8080/api/perfil");
        const dado = await res.json();

        console.log("PERFIL FRONT: ", dado.perfil);

        if(!dado.perfil || dado.perfil.toLowerCase() !== "admin") {
            document.querySelectorAll(".btn-menu").forEach(btn => { btn.style.display = "none"; });
        }
    } catch (e) {
        console.error("Erro ao veriricar o perfil.", e);   
    }

}

validarLogin();