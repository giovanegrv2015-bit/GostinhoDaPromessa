async function validarLogin() {
    try {
        const res = await fetch("http://localhost:8080/api/login");
        const dado = await res.json();

        console.log("PERFIL FRONT: ", dado.perfil);

        if(!dado.perfil || dado.perfil.toLowerCase() !== "admin") {
            document.getElementsByClassName(".btn-menu").style.display = "none";
        }
    } catch (e) {
        console.error("Erro ao veriricar o perfil.", e);   
    }

}

validarLogin();