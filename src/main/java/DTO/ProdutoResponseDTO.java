package DTO;

import enums.CategoriaPedido;

import java.math.BigDecimal;
import java.util.List;

public record ProdutoResponseDTO(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        Integer quantidadeEstoque,
        Boolean ativo,
        CategoriaPedido categoria,
        CategoriaPedido categoriaPedido,
        List<Long> itensPedidoIds
) {
}
