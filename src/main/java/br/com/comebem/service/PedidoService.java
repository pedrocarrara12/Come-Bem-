package br.com.comebem.service;

import br.com.comebem.dto.ItemPedidoRequestDTO;
import br.com.comebem.dto.ItemPedidoResponseDTO;
import br.com.comebem.dto.PedidoRequestDTO;
import br.com.comebem.dto.PedidoResponseDTO;
import br.com.comebem.entity.ItemPedido;
import br.com.comebem.entity.Mesa;
import br.com.comebem.entity.Pedido;
import br.com.comebem.entity.Produto;
import br.com.comebem.enums.StatusMesa;
import br.com.comebem.enums.StatusPedido;
import br.com.comebem.exception.MesaIndisponivelException;
import br.com.comebem.exception.PedidoNaoEncontradoException;
import br.com.comebem.exception.ProdutoInativoException;
import br.com.comebem.exception.RegraDeNegocioException;
import br.com.comebem.repository.ItemPedidoRepository;
import br.com.comebem.repository.MesaRepository;
import br.com.comebem.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PedidoService {

    private static final EnumSet<StatusPedido> PEDIDOS_ATIVOS = EnumSet.of(
            StatusPedido.ABERTO,
            StatusPedido.EM_PREPARO,
            StatusPedido.PRONTO,
            StatusPedido.ENTREGUE
    );

    private static final Map<StatusPedido, Set<StatusPedido>> TRANSICOES = Map.of(
            StatusPedido.ABERTO, EnumSet.of(StatusPedido.EM_PREPARO, StatusPedido.CANCELADO),
            StatusPedido.EM_PREPARO, EnumSet.of(StatusPedido.PRONTO, StatusPedido.CANCELADO),
            StatusPedido.PRONTO, EnumSet.of(StatusPedido.ENTREGUE),
            StatusPedido.ENTREGUE, EnumSet.of(StatusPedido.FINALIZADO),
            StatusPedido.CANCELADO, EnumSet.noneOf(StatusPedido.class),
            StatusPedido.FINALIZADO, EnumSet.noneOf(StatusPedido.class)
    );

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final MesaRepository mesaRepository;
    private final ClienteService clienteService;
    private final MesaService mesaService;
    private final ProdutoService produtoService;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ItemPedidoRepository itemPedidoRepository,
            MesaRepository mesaRepository,
            ClienteService clienteService,
            MesaService mesaService,
            ProdutoService produtoService
    ) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.mesaRepository = mesaRepository;
        this.clienteService = clienteService;
        this.mesaService = mesaService;
        this.produtoService = produtoService;
    }

    @Transactional
    public PedidoResponseDTO criar(PedidoRequestDTO dto) {
        Mesa mesa = mesaService.obter(dto.mesaId());
        if (mesa.getStatus() == StatusMesa.INATIVA) {
            throw new MesaIndisponivelException("Nao e permitido abrir pedido em mesa inativa");
        }

        Pedido pedido = new Pedido(clienteService.obter(dto.clienteId()), mesa);
        dto.itens().forEach(item -> pedido.adicionarItem(criarItem(item)));
        mesa.setStatus(StatusMesa.OCUPADA);
        mesaRepository.save(mesa);
        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listar() {
        return pedidoRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPorId(Long id) {
        return toResponse(obter(id));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPorStatus(StatusPedido status) {
        return pedidoRepository.findByStatus(status).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPorCliente(Long clienteId) {
        clienteService.obter(clienteId);
        return pedidoRepository.findByClienteId(clienteId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPorMesa(Long mesaId) {
        mesaService.obter(mesaId);
        return pedidoRepository.findByMesaId(mesaId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public PedidoResponseDTO atualizarStatus(Long id, StatusPedido novoStatus) {
        Pedido pedido = obter(id);
        validarTransicao(pedido, novoStatus);
        if (novoStatus == StatusPedido.FINALIZADO && pedido.getItens().isEmpty()) {
            throw new RegraDeNegocioException("Nao e permitido finalizar pedido sem itens");
        }
        pedido.setStatus(novoStatus);
        if (novoStatus == StatusPedido.CANCELADO || novoStatus == StatusPedido.FINALIZADO) {
            liberarMesaSeNecessario(pedido);
        }
        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public PedidoResponseDTO adicionarItem(Long pedidoId, ItemPedidoRequestDTO dto) {
        Pedido pedido = obter(pedidoId);
        validarPedidoEditavel(pedido);
        pedido.adicionarItem(criarItem(dto));
        return toResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public void removerItem(Long pedidoId, Long itemId) {
        Pedido pedido = obter(pedidoId);
        validarPedidoEditavel(pedido);
        ItemPedido item = itemPedidoRepository.findByIdAndPedidoId(itemId, pedidoId)
                .orElseThrow(() -> new RegraDeNegocioException("Item nao pertence ao pedido informado"));
        pedido.removerItem(item);
        pedidoRepository.save(pedido);
    }

    @Transactional
    public PedidoResponseDTO cancelar(Long id) {
        return atualizarStatus(id, StatusPedido.CANCELADO);
    }

    @Transactional
    public PedidoResponseDTO finalizar(Long id) {
        return atualizarStatus(id, StatusPedido.FINALIZADO);
    }

    private Pedido obter(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException("Pedido nao encontrado"));
    }

    private ItemPedido criarItem(ItemPedidoRequestDTO dto) {
        Produto produto = produtoService.obter(dto.produtoId());
        if (!produto.isAtivo()) {
            throw new ProdutoInativoException("Produto inativo nao pode ser adicionado ao pedido");
        }
        return new ItemPedido(produto, dto.quantidade());
    }

    private void validarPedidoEditavel(Pedido pedido) {
        if (pedido.getStatus() == StatusPedido.FINALIZADO || pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new RegraDeNegocioException("Pedido finalizado ou cancelado nao pode ser alterado");
        }
    }

    private void validarTransicao(Pedido pedido, StatusPedido novoStatus) {
        if (!TRANSICOES.get(pedido.getStatus()).contains(novoStatus)) {
            throw new RegraDeNegocioException(
                    "Transicao de status invalida: " + pedido.getStatus() + " para " + novoStatus
            );
        }
    }

    private void liberarMesaSeNecessario(Pedido pedido) {
        if (!pedidoRepository.existsByMesaIdAndStatusInAndIdNot(
                pedido.getMesa().getId(),
                PEDIDOS_ATIVOS,
                pedido.getId()
        )) {
            pedido.getMesa().setStatus(StatusMesa.LIVRE);
            mesaRepository.save(pedido.getMesa());
        }
    }

    private PedidoResponseDTO toResponse(Pedido pedido) {
        List<ItemPedidoResponseDTO> itens = pedido.getItens().stream()
                .map(item -> new ItemPedidoResponseDTO(
                        item.getId(),
                        item.getProduto().getId(),
                        item.getProduto().getNome(),
                        item.getQuantidade(),
                        item.getPrecoUnitario(),
                        item.getSubtotal()
                ))
                .toList();
        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getCliente().getId(),
                pedido.getCliente().getNome(),
                pedido.getMesa().getId(),
                pedido.getMesa().getNumero(),
                itens,
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getDataCriacao(),
                pedido.getDataAtualizacao()
        );
    }
}
