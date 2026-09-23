-- Roda UMA vez, quando o volume do banco é criado (docker compose down -v apaga o volume e os dados).
-- Aqui só existe estrutura. Os usuários iniciais vêm do .env (ADMIN_USER / ADMIN_PASSWORD / SEED_DEMO):
-- senha escrita neste arquivo é senha pública, porque o repositório é público.

use estoque_db;

create table users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    psw VARCHAR(100) NOT NULL,
    nameFirst varchar(50),
    sobreNome varchar(50),
    matricula varchar(50),
    CPF varchar(20),
    sexo varchar(50),
    dtaNascimento date,
    email varchar(50),
    telefone varchar(25),
    funcao varchar(50),
    CEP varchar(50),
    endereco varchar(50),
    numero varchar(20),
    complemento varchar(50),
    bairro varchar(50),
    cidade varchar(50),
    estado varchar(20),
    -- dois usuários com o mesmo nome: no login só o primeiro entrava
    CONSTRAINT uk_users_username UNIQUE (username)
);

create table itens (
    id int auto_increment primary key,
    -- opcional: só é preenchido quando houver leitor de código de barras
    codigo_barras varchar(100),
    nome_item varchar(100) not null,
    marca varchar(100),
    data_fabricacao date,
    data_vencimento date,
    quantidade BIGINT,
    valor decimal(10,2),
    total decimal(10,2),
    status varchar(50),
    local varchar(100),
    categoria varchar(100),
    estoque_minimo BIGINT NOT NULL DEFAULT 0
);
