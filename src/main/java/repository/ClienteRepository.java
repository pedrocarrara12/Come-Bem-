package repository;

import entity.Cliente;
import entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    List<Cliente> findByNomeContainingIgnoreCase(String nome);

    void deleteByCpf(String cpf);
}
