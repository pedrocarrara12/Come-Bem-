package br.com.comebem.service;

import br.com.comebem.dto.MesaRequestDTO;
import br.com.comebem.dto.MesaResponseDTO;
import br.com.comebem.entity.Mesa;
import br.com.comebem.enums.StatusMesa;
import br.com.comebem.enums.StatusPedido;
import br.com.comebem.exception.MesaNaoEncontradaException;
import br.com.comebem.exception.RegraDeNegocioException;
import br.com.comebem.repository.MesaRepository;
import br.com.comebem.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;

@Service
public class MesaService {

    private static final EnumSet<StatusPedido> PEDIDOS_ATIVOS = EnumSet.of(
            StatusPedido.ABERTO,
            StatusPedido.EM_PREPARO,
            StatusPedido.PRONTO,
            StatusPedido.ENTREGUE
    );

    private final MesaRepository mesaRepository;
    private final PedidoRepository pedidoRepository;

    public MesaService(MesaRepository mesaRepository, PedidoRepository pedidoRepository) {
        this.mesaRepository = mesaRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    public MesaResponseDTO criar(MesaRequestDTO dto) {
        validarNumeroDisponivel(dto.numero(), null);
        return toResponse(mesaRepository.save(new Mesa(dto.numero(), dto.capacidade(), dto.status())));
    }

    @Transactional(readOnly = true)
    public List<MesaResponseDTO> listar() {
        return mesaRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MesaResponseDTO buscarPorId(Long id) {
        return toResponse(obter(id));
    }

    @Transactional(readOnly = true)
    public MesaResponseDTO buscarPorNumero(Integer numero) {
        return toResponse(mesaRepository.findByNumero(numero)
                .orElseThrow(() -> new MesaNaoEncontradaException("Mesa nao encontrada")));
    }

    @Transactional(readOnly = true)
    public List<MesaResponseDTO> listarPorStatus(StatusMesa status) {
        return mesaRepository.findByStatus(status).stream().map(this::toResponse).toList();
    }

    @Transactional
    public MesaResponseDTO atualizar(Long id, MesaRequestDTO dto) {
        Mesa mesa = obter(id);
        validarNumeroDisponivel(dto.numero(), id);
        mesa.setNumero(dto.numero());
        mesa.setCapacidade(dto.capacidade());
        if (dto.status() != null && dto.status() != mesa.getStatus()) {
            validarAlteracaoStatus(mesa, dto.status());
            mesa.setStatus(dto.status());
        }
        return toResponse(mesaRepository.save(mesa));
    }

    @Transactional
    public MesaResponseDTO atualizarStatus(Long id, StatusMesa status) {
        Mesa mesa = obter(id);
        validarAlteracaoStatus(mesa, status);
        mesa.setStatus(status);
        return toResponse(mesaRepository.save(mesa));
    }

    @Transactional
    public void excluir(Long id) {
        Mesa mesa = obter(id);
        if (pedidoRepository.existsByMesaIdAndStatusIn(id, PEDIDOS_ATIVOS)) {
            throw new RegraDeNegocioException("Nao e permitido excluir mesa com pedido em aberto");
        }
        mesaRepository.delete(mesa);
    }

    Mesa obter(Long id) {
        return mesaRepository.findById(id)
                .orElseThrow(() -> new MesaNaoEncontradaException("Mesa nao encontrada"));
    }

    private void validarNumeroDisponivel(Integer numero, Long mesaId) {
        mesaRepository.findByNumero(numero)
                .filter(mesa -> mesaId == null || !mesa.getId().equals(mesaId))
                .ifPresent(mesa -> {
                    throw new RegraDeNegocioException("Numero de mesa ja cadastrado");
                });
    }

    private void validarAlteracaoStatus(Mesa mesa, StatusMesa status) {
        if (status != StatusMesa.OCUPADA
                && pedidoRepository.existsByMesaIdAndStatusIn(mesa.getId(), PEDIDOS_ATIVOS)) {
            throw new RegraDeNegocioException("Mesa com pedido ativo deve permanecer ocupada");
        }
    }

    private MesaResponseDTO toResponse(Mesa mesa) {
        return new MesaResponseDTO(mesa.getId(), mesa.getNumero(), mesa.getCapacidade(), mesa.getStatus());
    }
}
