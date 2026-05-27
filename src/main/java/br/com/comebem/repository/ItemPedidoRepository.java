package br.com.comebem.repository;

import br.com.comebem.entity.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    boolean existsByProdutoId(Long produtoId);

    Optional<ItemPedido> findByIdAndPedidoId(Long id, Long pedidoId);
}
