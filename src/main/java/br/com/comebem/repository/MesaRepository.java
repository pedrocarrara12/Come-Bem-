package br.com.comebem.repository;

import br.com.comebem.entity.Mesa;
import br.com.comebem.enums.StatusMesa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MesaRepository extends JpaRepository<Mesa, Long> {

    boolean existsByNumero(Integer numero);

    Optional<Mesa> findByNumero(Integer numero);

    List<Mesa> findByStatus(StatusMesa status);
}
