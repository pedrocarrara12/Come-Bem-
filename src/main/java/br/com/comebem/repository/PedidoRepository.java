package br.com.comebem.repository;

import br.com.comebem.entity.Pedido;
import br.com.comebem.enums.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    boolean existsByClienteId(Long clienteId);

    boolean existsByMesaIdAndStatusIn(Long mesaId, Collection<StatusPedido> status);

    boolean existsByMesaIdAndStatusInAndIdNot(Long mesaId, Collection<StatusPedido> status, Long id);

    List<Pedido> findByStatus(StatusPedido status);

    List<Pedido> findByClienteId(Long clienteId);

    List<Pedido> findByMesaId(Long mesaId);
}
