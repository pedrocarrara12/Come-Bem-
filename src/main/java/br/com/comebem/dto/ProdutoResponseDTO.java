package br.com.comebem.dto;

import br.com.comebem.enums.CategoriaProduto;

import java.math.BigDecimal;

public record ProdutoResponseDTO(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        CategoriaProduto categoria,
        boolean ativo
) {
}
