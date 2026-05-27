package br.com.comebem.service;

import br.com.comebem.dto.ProdutoRequestDTO;
import br.com.comebem.dto.ProdutoResponseDTO;
import br.com.comebem.entity.Produto;
import br.com.comebem.enums.CategoriaProduto;
import br.com.comebem.exception.ProdutoNaoEncontradoException;
import br.com.comebem.repository.ItemPedidoRepository;
import br.com.comebem.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public ProdutoService(ProdutoRepository produtoRepository, ItemPedidoRepository itemPedidoRepository) {
        this.produtoRepository = produtoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    @Transactional
    public ProdutoResponseDTO criar(ProdutoRequestDTO dto) {
        boolean ativo = dto.ativo() == null || dto.ativo();
        Produto produto = new Produto(dto.nome(), dto.descricao(), dto.preco(), dto.categoria(), ativo);
        return toResponse(produtoRepository.save(produto));
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> listar() {
        return produtoRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponseDTO buscarPorId(Long id) {
        return toResponse(obter(id));
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> listarPorCategoria(CategoriaProduto categoria) {
        return produtoRepository.findByCategoria(categoria).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> listarAtivos() {
        return produtoRepository.findByAtivoTrue().stream().map(this::toResponse).toList();
    }

    @Transactional
    public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO dto) {
        Produto produto = obter(id);
        produto.setNome(dto.nome());
        produto.setDescricao(dto.descricao());
        produto.setPreco(dto.preco());
        produto.setCategoria(dto.categoria());
        if (dto.ativo() != null) {
            produto.setAtivo(dto.ativo());
        }
        return toResponse(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoResponseDTO ativar(Long id) {
        Produto produto = obter(id);
        produto.setAtivo(true);
        return toResponse(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoResponseDTO inativar(Long id) {
        Produto produto = obter(id);
        produto.setAtivo(false);
        return toResponse(produtoRepository.save(produto));
    }

    @Transactional
    public void excluir(Long id) {
        Produto produto = obter(id);
        if (itemPedidoRepository.existsByProdutoId(id)) {
            produto.setAtivo(false);
            produtoRepository.save(produto);
            return;
        }
        produtoRepository.delete(produto);
    }

    Produto obter(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNaoEncontradoException("Produto nao encontrado"));
    }

    private ProdutoResponseDTO toResponse(Produto produto) {
        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getCategoria(),
                produto.isAtivo()
        );
    }
}
