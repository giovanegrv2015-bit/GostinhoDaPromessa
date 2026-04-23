async function filtroEstoque(params) {
    try {
    const response = await fetch("http://localhost:8080/api/estoque");
    const dados = await response.json();

    const nome = document.getElementeById("pesquisarNome").value.toLowerCase();
    const tipo = document.getElementeById("tipoMovimentacao").value;
    const data = document.getElementeById("filtroData").value;

    const tabela = document.getElementById("corpoTabela");
    tabela.innerHTML = "";

     const filtrados = dados.filter(item => {
        const matchNome = nome === "" ||item.nomeProduto.toLowerCase().includes(nome);
        const matchTipo = tipo === "" || item.status === tipo;
        const matchData = data === "" || item.dataFabricacao === data;

        return matchNome && matchTipo && matchData;
    });

    filtrados.forEach(item =>{
        const linha = `
            <tr>
                <td>${item.codigoBarras}</td>
                <td>${item.nomeProduto}</td>
                <td>${item.fabricante}</td>
                <td>${item.marca}</td>
                <td>${item.quantidade}</td>
                <td>${parseFloat(item.valor).toFixed(2)}</td>
                <td>${parseFloat(item.total).toFixed(2)}</td>
                <td>${item.status}</td>
            </tr>
        `;
        tabela.innerHTML += linha;
    });

    } catch (error) {
        console.error("Erro ao filtrar", erro);
    }    
}
