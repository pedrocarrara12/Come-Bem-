package br.com.comebem.service;

import br.com.comebem.dto.AtendimentoIaRequestDTO;
import br.com.comebem.dto.AtendimentoIaResponseDTO;
import br.com.comebem.exception.RegraDeNegocioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
public class AtendimentoIaService {

    private static final String PROMPT_ATENDIMENTO = """
            Voce e o atendente virtual do restaurante ComeBem.
            Responda em portugues brasileiro, de forma educada, objetiva e simples.
            Ajude com duvidas sobre cardapio, pedidos, mesas e atendimento.
            Nao invente precos, disponibilidade, dados de clientes ou status de pedidos.
            Quando precisar de uma informacao que depende do sistema, diga que ela deve ser consultada no ComeBem.
            """;

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public AtendimentoIaService(
            RestClient.Builder restClientBuilder,
            @Value("${gemini.base-url}") String baseUrl,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    public AtendimentoIaResponseDTO responder(AtendimentoIaRequestDTO dto) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RegraDeNegocioException("Atendimento por IA nao configurado. Defina GEMINI_API_KEY no ambiente.");
        }

        GeminiRequest request = new GeminiRequest(
                new Content(List.of(new Part(PROMPT_ATENDIMENTO))),
                List.of(new Content(List.of(new Part(dto.mensagem())))),
                new GenerationConfig(0.4, 500)
        );

        try {
            GeminiResponse response = restClient.post()
                    .uri("/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);

            String resposta = extrairTexto(response);
            return new AtendimentoIaResponseDTO(resposta);
        } catch (RestClientException ex) {
            throw new RegraDeNegocioException("Nao foi possivel consultar o atendimento por IA no momento.");
        }
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
