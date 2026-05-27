package br.com.comebem.repository;

import br.com.comebem.entity.Produto;
import br.com.comebem.enums.CategoriaProduto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByAtivoTrue();

    List<Produto> findByCategoria(CategoriaProduto categoria);
}
