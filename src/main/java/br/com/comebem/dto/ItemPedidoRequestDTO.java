package br.com.comebem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemPedidoRequestDTO(
        @NotNull(message = "O produto e obrigatorio")
        @Positive(message = "O produto deve ser valido")
        Long produtoId,

        @NotNull(message = "A quantidade e obrigatoria")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantidade
) {
}
