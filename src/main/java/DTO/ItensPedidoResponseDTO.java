package DTO;

import java.math.BigDecimal;

public record ItensPedidoResponseDTO(
        Long id,
        Long pedidoId,
        Long produtoId,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal
) {
}
