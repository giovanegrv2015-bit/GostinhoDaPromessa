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
        numero int,
	complemento varchar(50),
	bairro varchar(50),
        cidade varchar (50),
	estado varchar(20)
);

create table produtos (
        id int auto_increment primary key,
        codigo_barras varchar(100) not null,
        nome_produto varchar(100) not null,
        fabricante varchar(100),
        marca varchar(100),
        data_fabricacao date,
        data_vencimento date,
        quantidade BIGINT,
        valor decimal(10,2),
        total decimal(10,2),
        status varchar(50)
);
