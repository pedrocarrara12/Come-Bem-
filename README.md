# ComeBem API

Sistema de gestao para restaurante desenvolvido com Java e Spring Boot. O projeto controla clientes, mesas, cardapio e pedidos em um fluxo completo de atendimento, com regras de negocio no backend, banco PostgreSQL, painel web responsivo e um agente de atendimento por IA integrado ao Gemini.

A ideia do ComeBem e simular um ambiente real de restaurante: o atendente abre pedidos, acompanha o status da cozinha, gerencia mesas, mantem o cardapio atualizado e consulta informacoes operacionais em uma interface simples.

## Destaques Do Projeto

- API REST com arquitetura em camadas.
- Regras de negocio centralizadas em services.
- Persistencia com Spring Data JPA e PostgreSQL.
- Validacao de entrada com Bean Validation.
- Tratamento padronizado de erros.
- Frontend web servido pelo proprio Spring Boot.
- Docker Compose para subir API e banco.
- Swagger UI para documentacao interativa.
- Agente de atendimento com Gemini API.
- Leitura segura de variaveis por `.env`, sem versionar segredos.

## Como Funciona

O sistema foi dividido em camadas para deixar cada parte com uma responsabilidade clara:

| Camada | Papel no projeto |
| --- | --- |
| `controller` | Recebe as requisicoes HTTP e define os contratos da API. |
| `dto` | Transporta dados de entrada e saida sem expor diretamente as entidades. |
| `service` | Concentra as regras de negocio, validacoes e transacoes. |
| `repository` | Faz o acesso ao banco usando Spring Data JPA. |
| `entity` | Representa as tabelas e relacionamentos do dominio. |
| `exception` | Padroniza os erros retornados pela API. |

Na pratica, isso facilita manutencao, testes e evolucao. Por exemplo: uma regra como "mesa com pedido ativo nao pode ser liberada manualmente" fica no service, nao espalhada pela interface ou pelo banco.

## Tecnologias

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Bean Validation
- PostgreSQL 16
- Maven Wrapper
- Docker Compose
- Springdoc OpenAPI / Swagger UI
- HTML, CSS e JavaScript vanilla
- Gemini API

## Funcionalidades

### Clientes

- Cadastro, consulta, atualizacao e exclusao.
- CPF obrigatorio e unico.
- Bloqueio de exclusao quando o cliente possui pedidos vinculados.

### Mesas

- Cadastro de mesas com numero, capacidade e status.
- Consulta por status.
- Controle de disponibilidade do salao.
- Bloqueio de exclusao de mesa com pedido ativo.

### Cardapio

- Cadastro de produtos por categoria.
- Ativacao e inativacao de itens.
- Exclusao segura: se o produto ja foi usado em pedido, ele e inativado em vez de removido.

### Pedidos

- Abertura de pedido com cliente, mesa e itens.
- Calculo automatico de subtotal e total.
- Inclusao e remocao de itens enquanto o pedido esta em atendimento.
- Fluxo controlado de status.
- Cancelamento e finalizacao com regras de consistencia.

## Fluxo De Status Do Pedido

```text
ABERTO -> EM_PREPARO -> PRONTO -> ENTREGUE -> FINALIZADO
```

Pedidos `ABERTO` e `EM_PREPARO` tambem podem ser cancelados.

Um pedido `FINALIZADO` ou `CANCELADO` nao pode receber alteracoes de itens. Essa regra protege a consistencia financeira do atendimento.

## Agente De Atendimento Com IA

O projeto inclui um agente de atendimento integrado ao Gemini. Ele fica disponivel no frontend como um bot no canto da tela e tambem pode ser consumido pelo endpoint:

```http
POST /atendimento/ia
Content-Type: application/json
```

Exemplo:

```json
{
  "mensagem": "Qual o status da mesa 4?"
}
```

Resposta:

```json
{
  "resposta": "A mesa 4 esta livre e possui capacidade para 4 pessoas."
}
```

### O ponto tecnico mais importante

A IA nao acessa o banco diretamente.

O fluxo e:

1. O usuario envia uma pergunta em linguagem natural.
2. O Gemini classifica a intencao em uma acao permitida.
3. O backend executa a consulta real usando os services da aplicacao.
4. O resultado e enviado como contexto para o Gemini.
5. A IA responde de forma amigavel para o usuario.

Isso deixa o agente mais flexivel sem abrir mao de seguranca. A IA pode interpretar frases como:

- `Como esta o pedido 32?`
- `Tem pedidos prontos?`
- `Quais mesas estao livres?`
- `Me mostra as bebidas`
- `Quais produtos tem no cardapio?`
- `Consulta o cliente 3`

Mas ela so pode acionar consultas previamente autorizadas pelo backend, como buscar pedido, listar mesas por status, listar produtos ativos ou buscar cliente.

## Seguranca De Configuracao

Segredos nao devem ser versionados. A chave do Gemini fica no arquivo `.env`, que e ignorado pelo Git.

Exemplo:

```env
GEMINI_API_KEY=sua_chave_aqui
GEMINI_MODEL=gemini-2.5-flash
GEMINI_BASE_URL=https://generativelanguage.googleapis.com/v1beta
```

O Spring carrega esse arquivo localmente com:

```properties
spring.config.import=optional:file:.env[.properties]
```

O repositorio mantem apenas o `.env.example`, com placeholders.

## Frontend

O frontend e uma SPA simples, sem etapa de build, servida em:

```text
src/main/resources/static
```

Ele consome os endpoints REST da propria API e permite:

- Visualizar metricas do restaurante.
- Gerenciar pedidos.
- Gerenciar mesas.
- Gerenciar produtos do cardapio.
- Gerenciar clientes.
- Consultar o agente de IA pelo bot fixo na tela.

Essa escolha deixa o deploy mais simples: backend e frontend sobem juntos.

## Como Rodar Localmente

Requisitos:

- Java 21
- Docker

1. Crie o arquivo de configuracao local:

```bash
cp .env.example .env
```

No Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

2. Preencha as variaveis do `.env`, incluindo `GEMINI_API_KEY` se quiser usar o agente de IA.

3. Suba tudo com Docker Compose:

```bash
docker compose --env-file .env up --build
```

No Windows PowerShell:

```powershell
docker compose --env-file .env up --build
```

A aplicacao fica disponivel em:

```text
http://localhost:8080
```

A documentacao interativa fica em:

```text
http://localhost:8080/swagger-ui.html
```

## Rodando Pela IDE Ou Maven

Se quiser rodar apenas o banco no Docker:

```bash
docker compose --env-file .env up -d postgres
```

Depois execute:

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Como o projeto importa `optional:file:.env[.properties]`, as variaveis locais sao carregadas automaticamente quando o comando e executado a partir da raiz do projeto.

## Endpoints Principais

| Recurso | Endpoints |
| --- | --- |
| Clientes | `POST /clientes`, `GET /clientes`, `GET /clientes/{id}`, `GET /clientes/cpf/{cpf}`, `PUT /clientes/{id}`, `DELETE /clientes/{id}` |
| Mesas | `POST /mesas`, `GET /mesas`, `GET /mesas/{id}`, `GET /mesas/status/{status}`, `PUT /mesas/{id}`, `PATCH /mesas/{id}/status`, `DELETE /mesas/{id}` |
| Produtos | `POST /produtos`, `GET /produtos`, `GET /produtos/{id}`, `GET /produtos/categoria/{categoria}`, `GET /produtos/ativos`, `PUT /produtos/{id}`, `PATCH /produtos/{id}/ativar`, `PATCH /produtos/{id}/inativar`, `DELETE /produtos/{id}` |
| Pedidos | `POST /pedidos`, `GET /pedidos`, `GET /pedidos/{id}`, `GET /pedidos/status/{status}`, `GET /pedidos/cliente/{clienteId}`, `GET /pedidos/mesa/{mesaId}`, `PATCH /pedidos/{id}/status`, `POST /pedidos/{id}/itens`, `DELETE /pedidos/{id}/itens/{itemId}`, `POST /pedidos/{id}/cancelar`, `POST /pedidos/{id}/finalizar` |
| Atendimento IA | `POST /atendimento/ia` |

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

Formato de erro:

```json
{
  "timestamp": "2026-05-27T10:15:30",
  "status": 409,
  "erro": "Conflict",
  "mensagem": "Produto inativo nao pode ser adicionado ao pedido",
  "path": "/pedidos"
}
```

## Testes

Execute:

```bash
./mvnw test
```

Os testes de integracao usam PostgreSQL para se aproximar do ambiente real da aplicacao.

Para subir um banco temporario de teste:

```bash
docker compose --profile test up -d postgres-test
./mvnw test
docker compose --profile test down
```

## O Que Este Projeto Demonstra

- Modelagem de dominio com regras reais.
- Separacao de responsabilidades em uma API Spring.
- Uso de DTOs para entrada e saida de dados.
- Validacao e tratamento consistente de erros.
- Integracao com banco relacional.
- Docker para ambiente local reproduzivel.
- Frontend integrado ao backend.
- Consumo de API externa de IA.
- Uso de IA com controle de acoes e sem acesso direto ao banco.
- Cuidados com variaveis sensiveis e versionamento.

## Proximas Melhorias

- Autenticacao e autorizacao com Spring Security.
- Perfis de usuario para gerente, atendente e cozinha.
- Migracoes versionadas com Flyway.
- Paginacao e filtros avancados.
- Observabilidade com logs e metricas.
- Pipeline CI com build e testes no GitHub Actions.
- Deploy em ambiente cloud.

## Autor

Desenvolvido por Pedro Figueiredo como projeto de portfolio backend com integracao de IA.
