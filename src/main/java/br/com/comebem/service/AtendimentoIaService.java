package br.com.comebem.service;

import br.com.comebem.dto.AtendimentoIaRequestDTO;
import br.com.comebem.dto.AtendimentoIaResponseDTO;
import br.com.comebem.dto.ClienteResponseDTO;
import br.com.comebem.dto.MesaResponseDTO;
import br.com.comebem.dto.PedidoResponseDTO;
import br.com.comebem.dto.ProdutoResponseDTO;
import br.com.comebem.enums.CategoriaProduto;
import br.com.comebem.enums.StatusMesa;
import br.com.comebem.enums.StatusPedido;
import br.com.comebem.exception.RegraDeNegocioException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class AtendimentoIaService {

    private static final String ACAO_NENHUMA = "NENHUMA";

    private static final String PROMPT_CLASSIFICACAO = """
            Voce classifica mensagens de atendimento do restaurante ComeBem.
            Responda somente JSON valido, sem markdown e sem explicacoes.

            Acoes permitidas:
            - BUSCAR_PEDIDO_ID: buscar pedido por id. Parametro: id.
            - LISTAR_PEDIDOS_STATUS: listar pedidos por status. Parametro: status. Valores: ABERTO, EM_PREPARO, PRONTO, ENTREGUE, CANCELADO, FINALIZADO.
            - BUSCAR_MESA_NUMERO: buscar mesa pelo numero. Parametro: numero.
            - LISTAR_MESAS_STATUS: listar mesas por status. Parametro: status. Valores: LIVRE, OCUPADA, RESERVADA, INATIVA.
            - BUSCAR_PRODUTO_ID: buscar produto por id. Parametro: id.
            - LISTAR_CARDAPIO: listar produtos ativos. Sem parametro.
            - LISTAR_PRODUTOS_CATEGORIA: listar produtos por categoria. Parametro: categoria. Valores: ENTRADA, PRATO_PRINCIPAL, BEBIDA, SOBREMESA, OUTROS.
            - BUSCAR_CLIENTE_ID: buscar cliente por id. Parametro: id.
            - BUSCAR_CLIENTE_CPF: buscar cliente por CPF. Parametro: cpf com 11 digitos.
            - NENHUMA: quando a mensagem nao pedir consulta ao sistema.

            Formato obrigatorio:
            {"acao":"NENHUMA","id":null,"numero":null,"status":null,"categoria":null,"cpf":null}

            Escolha apenas uma acao. Quando houver duvida, use NENHUMA.
            """;

    private static final String PROMPT_ATENDIMENTO = """
            Voce e o atendente virtual do restaurante ComeBem.
            Responda em portugues brasileiro, de forma educada, objetiva e simples.
            Ajude com duvidas sobre cardapio, pedidos, mesas e atendimento.
            Nao invente precos, disponibilidade, dados de clientes ou status de pedidos.
            Quando houver contexto do sistema, use apenas essas informacoes para responder.
            Quando precisar de uma informacao que nao veio no contexto, diga que ela deve ser consultada no ComeBem.
            """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final PedidoService pedidoService;
    private final MesaService mesaService;
    private final ProdutoService produtoService;
    private final ClienteService clienteService;
    private final String apiKey;
    private final String model;

    public AtendimentoIaService(
            PedidoService pedidoService,
            MesaService mesaService,
            ProdutoService produtoService,
            ClienteService clienteService,
            @Value("${gemini.base-url}") String baseUrl,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.objectMapper = new ObjectMapper();
        this.pedidoService = pedidoService;
        this.mesaService = mesaService;
        this.produtoService = produtoService;
        this.clienteService = clienteService;
        this.apiKey = apiKey;
        this.model = model;
    }

    public AtendimentoIaResponseDTO responder(AtendimentoIaRequestDTO dto) {
        validarApiKey();

        String contexto = montarContextoDoSistema(dto.mensagem());
        String mensagem = montarMensagemComContexto(dto.mensagem(), contexto);

        GeminiRequest request = new GeminiRequest(
                new Content(List.of(new Part(PROMPT_ATENDIMENTO))),
                List.of(new Content(List.of(new Part(mensagem)))),
                new GenerationConfig(0.4, 500)
        );

        String resposta = chamarGemini(request);
        return new AtendimentoIaResponseDTO(resposta);
    }

    private String montarContextoDoSistema(String mensagem) {
        ConsultaIa consulta = classificarConsulta(mensagem);
        if (ACAO_NENHUMA.equals(consulta.acao())) {
            return "";
        }

        return switch (consulta.acao()) {
            case "BUSCAR_PEDIDO_ID" -> consulta.id()
                    .map(pedidoService::buscarPorId)
                    .map(this::formatarPedido)
                    .orElse("");
            case "LISTAR_PEDIDOS_STATUS" -> consulta.status()
                    .flatMap(status -> converterEnum(StatusPedido.class, status))
                    .map(pedidoService::listarPorStatus)
                    .map(this::formatarPedidos)
                    .orElse("");
            case "BUSCAR_MESA_NUMERO" -> consulta.numero()
                    .map(mesaService::buscarPorNumero)
                    .map(this::formatarMesa)
                    .orElse("");
            case "LISTAR_MESAS_STATUS" -> consulta.status()
                    .flatMap(status -> converterEnum(StatusMesa.class, status))
                    .map(mesaService::listarPorStatus)
                    .map(this::formatarMesas)
                    .orElse("");
            case "BUSCAR_PRODUTO_ID" -> consulta.id()
                    .map(produtoService::buscarPorId)
                    .map(this::formatarProduto)
                    .orElse("");
            case "LISTAR_CARDAPIO" -> formatarCardapio(produtoService.listarAtivos());
            case "LISTAR_PRODUTOS_CATEGORIA" -> consulta.categoria()
                    .flatMap(categoria -> converterEnum(CategoriaProduto.class, categoria))
                    .map(produtoService::listarPorCategoria)
                    .map(this::formatarCardapio)
                    .orElse("");
            case "BUSCAR_CLIENTE_ID" -> consulta.id()
                    .map(clienteService::buscarPorId)
                    .map(this::formatarCliente)
                    .orElse("");
            case "BUSCAR_CLIENTE_CPF" -> consulta.cpf()
                    .map(clienteService::buscarPorCpf)
                    .map(this::formatarCliente)
                    .orElse("");
            default -> "";
        };
    }

    private ConsultaIa classificarConsulta(String mensagem) {
        GeminiRequest request = new GeminiRequest(
                new Content(List.of(new Part(PROMPT_CLASSIFICACAO))),
                List.of(new Content(List.of(new Part(mensagem)))),
                new GenerationConfig(0.0, 250)
        );

        String resposta = chamarGemini(request);
        return lerConsulta(resposta);
    }

    private ConsultaIa lerConsulta(String resposta) {
        try {
            JsonNode json = objectMapper.readTree(limparJson(resposta));
            String acao = json.path("acao").asText(ACAO_NENHUMA).toUpperCase(Locale.ROOT);
            return new ConsultaIa(
                    acao,
                    lerLong(json, "id"),
                    lerInteger(json, "numero"),
                    lerTexto(json, "status"),
                    lerTexto(json, "categoria"),
                    lerTexto(json, "cpf")
            );
        } catch (JsonProcessingException ex) {
            return ConsultaIa.nenhuma();
        }
    }

    private String chamarGemini(GeminiRequest request) {
        try {
            GeminiResponse response = restClient.post()
                    .uri("/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);

            return extrairTexto(response);
        } catch (RestClientException ex) {
            throw new RegraDeNegocioException("Nao foi possivel consultar o atendimento por IA no momento.");
        }
    }

    private void validarApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RegraDeNegocioException("Atendimento por IA nao configurado. Defina GEMINI_API_KEY no ambiente.");
        }
    }

    private String montarMensagemComContexto(String mensagem, String contexto) {
        if (contexto == null || contexto.isBlank()) {
            return mensagem;
        }

        return """
                Contexto do sistema:
                %s

                Mensagem do usuario:
                %s
                """.formatted(contexto, mensagem);
    }

    private String formatarPedidos(List<PedidoResponseDTO> pedidos) {
        if (pedidos.isEmpty()) {
            return "Pedidos: nenhum pedido encontrado para o filtro informado.";
        }

        return "Pedidos:\n" + String.join("\n", pedidos.stream()
                .limit(20)
                .map(pedido -> "- Pedido %d: status %s, mesa %d, total R$ %s".formatted(
                        pedido.id(),
                        pedido.status(),
                        pedido.mesaNumero(),
                        pedido.valorTotal()
                ))
                .toList());
    }

    private String formatarPedido(PedidoResponseDTO pedido) {
        return """
                Pedido:
                - ID: %d
                - Status: %s
                - Mesa: %d
                - Valor total: R$ %s
                - Criado em: %s
                - Atualizado em: %s
                """.formatted(
                pedido.id(),
                pedido.status(),
                pedido.mesaNumero(),
                pedido.valorTotal(),
                pedido.dataCriacao(),
                pedido.dataAtualizacao()
        );
    }

    private String formatarMesas(List<MesaResponseDTO> mesas) {
        if (mesas.isEmpty()) {
            return "Mesas: nenhuma mesa encontrada para o filtro informado.";
        }

        return "Mesas:\n" + String.join("\n", mesas.stream()
                .limit(20)
                .map(mesa -> "- Mesa %d: status %s, capacidade %d".formatted(
                        mesa.numero(),
                        mesa.status(),
                        mesa.capacidade()
                ))
                .toList());
    }

    private String formatarMesa(MesaResponseDTO mesa) {
        return """
                Mesa:
                - ID: %d
                - Numero: %d
                - Capacidade: %d
                - Status: %s
                """.formatted(mesa.id(), mesa.numero(), mesa.capacidade(), mesa.status());
    }

    private String formatarProduto(ProdutoResponseDTO produto) {
        return """
                Produto:
                - ID: %d
                - Nome: %s
                - Descricao: %s
                - Preco: R$ %s
                - Categoria: %s
                - Ativo: %s
                """.formatted(
                produto.id(),
                produto.nome(),
                produto.descricao(),
                produto.preco(),
                produto.categoria(),
                produto.ativo() ? "sim" : "nao"
        );
    }

    private String formatarCliente(ClienteResponseDTO cliente) {
        return """
                Cliente:
                - ID: %d
                - Nome: %s
                - Pedidos: %s
                """.formatted(cliente.id(), cliente.nome(), cliente.pedidoIds());
    }

    private String formatarCardapio(List<ProdutoResponseDTO> produtos) {
        if (produtos.isEmpty()) {
            return "Cardapio: nenhum produto encontrado.";
        }

        String produtosFormatados = String.join("\n", produtos.stream()
                .limit(20)
                .map(produto -> "- %s: R$ %s (%s)".formatted(
                        produto.nome(),
                        produto.preco(),
                        produto.categoria()
                ))
                .toList());

        return "Cardapio:\n" + produtosFormatados;
    }

    private <T extends Enum<T>> Optional<T> converterEnum(Class<T> enumClass, String valor) {
        String normalizado = normalizar(valor);
        for (T constante : enumClass.getEnumConstants()) {
            if (constante.name().equals(normalizado)) {
                return Optional.of(constante);
            }
        }
        return Optional.empty();
    }

    private String normalizar(String valor) {
        String semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private String limparJson(String resposta) {
        return resposta
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }

    private Optional<Long> lerLong(JsonNode json, String campo) {
        JsonNode valor = json.get(campo);
        if (valor == null || valor.isNull()) {
            return Optional.empty();
        }
        if (valor.isNumber()) {
            return Optional.of(valor.asLong());
        }
        try {
            return Optional.of(Long.valueOf(valor.asText()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private Optional<Integer> lerInteger(JsonNode json, String campo) {
        return lerLong(json, campo).map(Long::intValue);
    }

    private Optional<String> lerTexto(JsonNode json, String campo) {
        JsonNode valor = json.get(campo);
        if (valor == null || valor.isNull() || valor.asText().isBlank()) {
            return Optional.empty();
        }
        return Optional.of(valor.asText());
    }

    private String extrairTexto(GeminiResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            throw new RegraDeNegocioException("A IA nao retornou uma resposta valida.");
        }

        Content content = response.candidates().getFirst().content();
        if (content == null || content.parts() == null || content.parts().isEmpty()) {
            throw new RegraDeNegocioException("A IA nao retornou uma resposta valida.");
        }

        String texto = content.parts().getFirst().text();
        if (texto == null || texto.isBlank()) {
            throw new RegraDeNegocioException("A IA nao retornou uma resposta valida.");
        }

        return texto.trim();
    }

    private record ConsultaIa(
            String acao,
            Optional<Long> id,
            Optional<Integer> numero,
            Optional<String> status,
            Optional<String> categoria,
            Optional<String> cpf
    ) {
        private static ConsultaIa nenhuma() {
            return new ConsultaIa(
                    ACAO_NENHUMA,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty()
            );
        }
    }

    private record GeminiRequest(
            Content systemInstruction,
            List<Content> contents,
            GenerationConfig generationConfig
    ) {
    }

    private record GenerationConfig(
            Double temperature,
            Integer maxOutputTokens
    ) {
    }

    private record Content(
            List<Part> parts
    ) {
    }

    private record Part(
            String text
    ) {
    }

    private record GeminiResponse(
            List<Candidate> candidates
    ) {
    }

    private record Candidate(
            Content content
    ) {
    }
}
