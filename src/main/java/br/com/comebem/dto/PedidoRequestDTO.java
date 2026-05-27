package br.com.comebem.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record PedidoRequestDTO(
        @NotNull(message = "O cliente e obrigatorio")
        @Positive(message = "O cliente deve ser valido")
        Long clienteId,

        @NotNull(message = "A mesa e obrigatoria")
        @Positive(message = "A mesa deve ser valida")
        Long mesaId,

        @NotEmpty(message = "O pedido deve conter ao menos um item")
        List<@Valid ItemPedidoRequestDTO> itens
) {
}
