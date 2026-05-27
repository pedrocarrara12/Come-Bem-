package br.com.comebem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtendimentoIaRequestDTO(
        @NotBlank(message = "Mensagem e obrigatoria")
        @Size(max = 1000, message = "Mensagem deve ter no maximo 1000 caracteres")
        String mensagem
) {
}
