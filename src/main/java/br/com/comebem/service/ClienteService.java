package br.com.comebem.service;

import br.com.comebem.dto.ClienteRequestDTO;
import br.com.comebem.dto.ClienteResponseDTO;
import br.com.comebem.entity.Cliente;
import br.com.comebem.entity.Pedido;
import br.com.comebem.exception.ClienteJaCadastradoException;
import br.com.comebem.exception.ClienteNaoEncontradoException;
import br.com.comebem.exception.RegraDeNegocioException;
import br.com.comebem.repository.ClienteRepository;
import br.com.comebem.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;

    public ClienteService(ClienteRepository clienteRepository, PedidoRepository pedidoRepository) {
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    public ClienteResponseDTO criar(ClienteRequestDTO dto) {
        validarCpfDisponivel(dto.cpf(), null);
        Cliente cliente = new Cliente(dto.nome(), dto.cpf(), dto.telefone(), dto.email());
        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listar() {
        return clienteRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorId(Long id) {
        return toResponse(obter(id));
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorCpf(String cpf) {
        return toResponse(clienteRepository.findByCpf(cpf)
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente nao encontrado")));
    }

    @Transactional
    public ClienteResponseDTO atualizar(Long id, ClienteRequestDTO dto) {
        Cliente cliente = obter(id);
        validarCpfDisponivel(dto.cpf(), id);
        cliente.setNome(dto.nome());
        cliente.setCpf(dto.cpf());
        cliente.setTelefone(dto.telefone());
        cliente.setEmail(dto.email());
        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = obter(id);
        if (pedidoRepository.existsByClienteId(id)) {
            throw new RegraDeNegocioException("Nao e permitido excluir cliente com pedidos vinculados");
        }
        clienteRepository.delete(cliente);
    }

    Cliente obter(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente nao encontrado"));
    }

    private void validarCpfDisponivel(String cpf, Long clienteId) {
        clienteRepository.findByCpf(cpf).ifPresent(cliente -> {
            if (clienteId == null || !cliente.getId().equals(clienteId)) {
                throw new ClienteJaCadastradoException("CPF ja cadastrado");
            }
        });
    }

    private ClienteResponseDTO toResponse(Cliente cliente) {
        List<Long> pedidoIds = cliente.getPedidos().stream().map(Pedido::getId).toList();
        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getTelefone(),
                cliente.getEmail(),
                pedidoIds
        );
    }
}
