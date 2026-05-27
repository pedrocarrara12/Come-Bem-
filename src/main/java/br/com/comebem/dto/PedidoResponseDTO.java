package br.com.comebem.dto;

import br.com.comebem.enums.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponseDTO(
        Long id,
        Long clienteId,
        String clienteNome,
        Long mesaId,
        Integer mesaNumero,
        List<ItemPedidoResponseDTO> itens,
        StatusPedido status,
        BigDecimal valorTotal,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
