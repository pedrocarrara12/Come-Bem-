package DTO;

import enums.CategoriaPedido;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoDTO(
        @NotBlank(message = "O nome e obrigatorio")
        String nome,

        @Size(max = 1000, message = "A descricao deve ter no maximo 1000 caracteres")
        String descricao,

        @NotNull(message = "O preco e obrigatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "O preco deve ser maior que zero")
        BigDecimal preco,

        @NotNull(message = "A quantidade em estoque e obrigatoria")
        @PositiveOrZero(message = "A quantidade em estoque nao pode ser negativa")
        Integer quantidadeEstoque,

        Boolean ativo,

        @NotNull(message = "A categoria e obrigatoria")
        CategoriaPedido categoria,

        @NotNull(message = "A categoria do pedido e obrigatoria")
        CategoriaPedido categoriaPedido
) {
}
