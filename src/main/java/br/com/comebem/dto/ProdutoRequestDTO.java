package br.com.comebem.dto;

import br.com.comebem.enums.CategoriaProduto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoRequestDTO(
        @NotBlank(message = "O nome e obrigatorio")
        @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres")
        String nome,

        @Size(max = 1000, message = "A descricao deve ter no maximo 1000 caracteres")
        String descricao,

        @NotNull(message = "O preco e obrigatorio")
        @Positive(message = "O preco deve ser maior que zero")
        BigDecimal preco,

        @NotNull(message = "A categoria e obrigatoria")
        CategoriaProduto categoria,

        Boolean ativo
) {
}
