package DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ItensPedidoDTO(
        Long pedidoId,

        @NotNull(message = "O produto e obrigatorio")
        @Positive(message = "O produto deve ser valido")
        Long produtoId,

        @NotNull(message = "A quantidade e obrigatoria")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantidade,

        @NotNull(message = "O preco unitario e obrigatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "O preco unitario deve ser maior que zero")
        BigDecimal precoUnitario,

        @NotNull(message = "O subtotal e obrigatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "O subtotal deve ser maior que zero")
        BigDecimal subtotal
) {
}
