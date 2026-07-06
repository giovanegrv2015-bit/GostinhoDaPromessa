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
        cidade varchar (50),
	estado varchar(20)
);

create table itens (
        id int auto_increment primary key,
        codigo_barras varchar(100) not null,
        nome_item varchar(100) not null,
        fabricante varchar(100),
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

insert into users
(username, psw, nameFirst, sobreNome, funcao)
values
    ('admin', '$2a$10$b9rzx0Fv0Kget0H/P9d1vuKmjf6vsBIMxypG8GvgKwiKI3nr0AYkq', 'Giovane', 'Pereira', 'ADMIN'),
    ('gerente', '$2a$10$9EVbjXcvvmYnqeyNUtUGXujLx07uIZxNhcjyANoBol5oxXXY48fm2', 'Gerente', 'previa', 'GERENTE'),
    ('funcionario', '$2a$10$9EVbjXcvvmYnqeyNUtUGXujLx07uIZxNhcjyANoBol5oxXXY48fm2', 'Funcionario', 'previa', 'FUNCIONARIO'),
    ('visitante', '$2a$10$9EVbjXcvvmYnqeyNUtUGXujLx07uIZxNhcjyANoBol5oxXXY48fm2', 'Visitante', 'previa', 'VISITANTE');