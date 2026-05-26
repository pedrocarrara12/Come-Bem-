package repository;

import entity.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    Optional<Funcionario> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    List<Funcionario> findByNomeContainingIgnoreCase(String nome);
}
