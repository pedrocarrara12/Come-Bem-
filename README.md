# ComeBem API

API REST para gerenciamento do atendimento de um restaurante, desenvolvida em Java e Spring Boot com foco em regras de negocio, organizacao em camadas e uma base adequada para portifolio profissional.

## Objetivo

O ComeBem controla clientes, mesas, cardapio e pedidos desde a abertura ate a finalizacao ou cancelamento. Totais e subtotais sao calculados pela aplicacao, preservando a consistencia financeira do atendimento.

## Tecnologias

- Java 21
- Spring Boot 4
- Spring Web MVC, Spring Data JPA e Bean Validation
- PostgreSQL 16 para execucao local
- PostgreSQL tambem para testes de integracao
- Maven Wrapper
- Docker Compose
- Springdoc OpenAPI / Swagger UI
- Frontend web responsivo servido pelo Spring Boot
- Gemini API para atendimento virtual por IA

## Funcionalidades

- Cadastro, consulta, atualizacao e exclusao protegida de clientes.
- Gerenciamento de mesas e disponibilidade operacional.
- Cadastro e ativacao/inativacao de produtos do cardapio.
- Abertura de pedidos com itens e total calculado automaticamente.
- Inclusao e remocao de itens enquanto o pedido esta em atendimento.
- Evolucao controlada do status do pedido.
- Padrao consistente de erros de validacao e regras de negocio.

## Regras De Negocio

- O CPF do cliente e obrigatorio e unico; cliente com pedidos nao pode ser excluido.
- Numero de mesa e unico; mesa inativa nao recebe pedido.
- Ao abrir um pedido, a mesa passa para `OCUPADA`; ao encerrar o ultimo pedido ativo, volta a `LIVRE`.
- Produto deve ter preco positivo e estar ativo para ser incluido em pedido.
- Produto ja usado em pedido nao e removido: a operacao de exclusao o inativa.
- Um pedido nasce com ao menos um item; quantidade, preco unitario, subtotal e total sao validados ou calculados no backend.
- O fluxo permitido e `ABERTO -> EM_PREPARO -> PRONTO -> ENTREGUE -> FINALIZADO`.
- `ABERTO` e `EM_PREPARO` tambem podem transitar para `CANCELADO`.
- Pedido `FINALIZADO` ou `CANCELADO` nao aceita alteracao de itens.

## Como Rodar Localmente

Requisitos: Java 21 e Docker.

1. Crie sua configuracao local:

```bash
cp .env.example .env
```

No Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

2. Suba a aplicacao completa:

```bash
docker compose --env-file .env up --build
```

No Windows PowerShell:

```powershell
docker compose --env-file .env up --build
```

3. Alternativamente, para rodar apenas o banco no Docker e a API pela IDE:

```bash
docker compose --env-file .env up -d
```

Depois exporte as variaveis do `.env` no terminal ou configure-as na IDE e execute:

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

A API fica disponivel em `http://localhost:8080`. A documentacao interativa esta em `http://localhost:8080/swagger-ui.html`.

O painel web fica disponivel na raiz da aplicacao:

```text
http://localhost:8080
```

Ele consome os mesmos endpoints REST da API e permite operar clientes, mesas, cardapio e pedidos sem um servidor frontend separado.

## Atendimento Por IA

O backend inclui um endpoint simples para atendimento virtual usando Gemini API. A chave deve ficar apenas no ambiente local ou no provedor de deploy:

```env
GEMINI_API_KEY=sua_chave_aqui
GEMINI_MODEL=gemini-2.5-flash
```

Nunca versiona a chave real. O arquivo `.env.example` traz apenas placeholders.

Endpoint:

```http
POST /atendimento/ia
Content-Type: application/json
```

Corpo da requisicao:

```json
{
  "mensagem": "Quais informacoes voce pode consultar para mim?"
}
```

Resposta:

```json
{
  "resposta": "..."
}
```

## Docker

O arquivo `docker-compose.yml` fornece a API e o PostgreSQL. Credenciais do ambiente real nao devem ser versionadas; use variaveis `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT`, `APP_PORT` e `DATABASE_URL`.

Para desligar o banco:

```bash
docker compose down
```

Use `docker compose down -v` apenas quando quiser remover tambem os dados locais.

## Endpoints

| Recurso | Endpoints principais |
| --- | --- |
| Clientes | `POST /clientes`, `GET /clientes`, `GET /clientes/{id}`, `GET /clientes/cpf/{cpf}`, `PUT /clientes/{id}`, `DELETE /clientes/{id}` |
| Mesas | `POST /mesas`, `GET /mesas`, `GET /mesas/{id}`, `GET /mesas/status/{status}`, `PUT /mesas/{id}`, `PATCH /mesas/{id}/status`, `DELETE /mesas/{id}` |
| Produtos | `POST /produtos`, `GET /produtos`, `GET /produtos/{id}`, `GET /produtos/categoria/{categoria}`, `GET /produtos/ativos`, `PUT /produtos/{id}`, `PATCH /produtos/{id}/ativar`, `PATCH /produtos/{id}/inativar`, `DELETE /produtos/{id}` |
| Pedidos | `POST /pedidos`, `GET /pedidos`, `GET /pedidos/{id}`, `GET /pedidos/status/{status}`, `GET /pedidos/cliente/{clienteId}`, `GET /pedidos/mesa/{mesaId}`, `PATCH /pedidos/{id}/status`, `POST /pedidos/{id}/itens`, `DELETE /pedidos/{id}/itens/{itemId}`, `POST /pedidos/{id}/cancelar`, `POST /pedidos/{id}/finalizar` |
| Atendimento IA | `POST /atendimento/ia` |

## Frontend

O projeto inclui uma SPA sem etapa de build em `src/main/resources/static`, ideal para portifolio e deploy simples junto ao backend.

Funcionalidades da interface:

- Dashboard com pedidos ativos, mesas ocupadas, cardapio ativo e receita finalizada.
- Cadastros de clientes, mesas e produtos.
- Ativacao, inativacao e exclusao segura de produtos.
- Abertura de pedidos com cliente, mesa e multiplos itens.
- Avanco de status do pedido conforme o fluxo permitido.
- Cancelamento, finalizacao, adicao e remocao de itens.
- Mensagens de erro baseadas no retorno padronizado da API.

## Exemplos De Requisicoes

Criar cliente:

```json
{
  "nome": "Ana Souza",
  "cpf": "12345678901",
  "telefone": "65999990000",
  "email": "ana@email.com"
}
```

Criar mesa:

```json
{
  "numero": 1,
  "capacidade": 4
}
```

Criar produto:

```json
{
  "nome": "Risoto de cogumelos",
  "descricao": "Arroz arboreo com cogumelos frescos",
  "preco": 48.90,
  "categoria": "PRATO_PRINCIPAL",
  "ativo": true
}
```

Criar pedido:

```json
{
  "clienteId": 1,
  "mesaId": 1,
  "itens": [
    {
      "produtoId": 1,
      "quantidade": 2
    }
  ]
}
```

Alterar status:

```json
{
  "status": "EM_PREPARO"
}
```

Erros retornam o formato:

```json
{
  "timestamp": "2026-05-27T10:15:30",
  "status": 409,
  "erro": "Conflict",
  "mensagem": "Produto inativo nao pode ser adicionado ao pedido",
  "path": "/pedidos"
}
```

## Estrutura De Pacotes

```text
br.com.comebem
|-- controller   # contratos HTTP e codigos de resposta
|-- dto          # entradas e saidas da API
|-- entity       # mapeamento JPA
|-- enums        # estados e categorias do dominio
|-- exception    # erros e tratamento global
|-- repository   # persistencia Spring Data
`-- service      # regras de negocio e transacoes
```

## Testes

Execute:

```bash
./mvnw test
```

Os testes de integracao usam PostgreSQL para reproduzir o banco real da aplicacao. Configure um banco separado, por exemplo `comebem_test`, pelas variaveis `TEST_DATABASE_URL`, `TEST_POSTGRES_USER` e `TEST_POSTGRES_PASSWORD` antes de executar. Eles validam cadastro de cliente, criacao e totalizacao de pedido, bloqueio de produto inativo, fluxo de status e cancelamento.

O Compose disponibiliza um PostgreSQL isolado e temporario na porta `5433` para a suite:

```bash
docker compose --profile test up -d postgres-test
./mvnw test
docker compose --profile test down
```

## Proximas Melhorias

- Autenticacao e autorizacao com Spring Security e JWT.
- Perfis de usuario para gerente, atendente e cozinha.
- Migracoes versionadas com Flyway.
- Paginacao, filtros adicionais e observabilidade.
- Pipeline CI com build e testes no GitHub Actions.
- Container da aplicacao junto ao banco para publicacao.

## Autor

Desenvolvido por Pedro Figueiredo como projeto de portifolio backend.
