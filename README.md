# Gostinho da Promessa — Sistema de Estoque

Sistema web de controle de estoque desenvolvido para uma confeitaria,
com cadastro de itens, controle de quantidades e acesso autenticado.

Projeto final do curso de Desenvolvimento de Sistemas (SENAI), aprovado.

## Tecnologias

- **Backend:** Java, Servlets, arquitetura MVC + DAO
- **Banco:** MySQL em container Docker
- **Frontend:** HTML, CSS e JavaScript
- **Segurança:** senhas com hash BCrypt, PreparedStatements em toda a
  camada de persistência

## Funcionalidades

- Autenticação de usuário
- Cadastro, edição e exclusão de itens
- Listagem e consulta do estoque

## Sobre o desenvolvimento

A base inicial deste sistema foi construída em aula, acompanhando o professor
junto com a turma — por isso o histórico inclui commits de colegas do curso.

Toda a evolução a partir desse ponto é trabalho próprio: refatoração do modelo
de dados (Produtos → Itens, troca da chave de negócio para id), autenticação
com BCrypt, uso de PreparedStatements em toda a camada de persistência,
correção de bugs críticos de JDBC e containerização do MySQL com Docker.

## Status

Projeto concluído. Ainda não está em produção.
