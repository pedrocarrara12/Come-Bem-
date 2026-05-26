package repository;

import entity.Produto;
import enums.CategoriaPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByAtivoTrue();

    List<Produto> findByCategoriaAndAtivoTrue(CategoriaPedido categoria);

    List<Produto> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome);
}
