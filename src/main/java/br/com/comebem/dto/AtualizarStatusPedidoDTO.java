package br.com.comebem.dto;

import br.com.comebem.enums.StatusPedido;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusPedidoDTO(
        @NotNull(message = "O status do pedido e obrigatorio")
        StatusPedido status
) {
}
