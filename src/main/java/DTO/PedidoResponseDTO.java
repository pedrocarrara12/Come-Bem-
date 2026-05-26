package DTO;

import java.math.BigDecimal;
import java.util.List;

public record PedidoResponseDTO(
        Long id,
        Long clienteId,
        BigDecimal preco,
        List<ItensPedidoResponseDTO> itensPedidos,
        Long mesaId,
        Long funcionarioId
) {
}
