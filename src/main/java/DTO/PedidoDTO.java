package DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record PedidoDTO(
        @NotNull(message = "O cliente e obrigatorio")
        @Positive(message = "O cliente deve ser valido")
        Long clienteId,

        @NotNull(message = "O preco e obrigatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "O preco deve ser maior que zero")
        BigDecimal preco,

        @NotEmpty(message = "O pedido deve conter ao menos um item")
        List<@Valid ItensPedidoDTO> itensPedidos,

        @NotNull(message = "A mesa e obrigatoria")
        @Positive(message = "A mesa deve ser valida")
        Long mesaId,

        @Positive(message = "O funcionario deve ser valido")
        Long funcionarioId
) {
}
