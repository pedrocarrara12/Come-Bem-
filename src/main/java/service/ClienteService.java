package service;

import DTO.ClienteDTO;
import DTO.ClienteResponseDTO;
import entity.Cliente;
import entity.Pedido;
import exceptions.ClienteCadastradoException;
import exceptions.ClienteComPedidoVinculadoException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import repository.ClienteRepository;
import repository.PedidoRepository;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;

    public ClienteService(ClienteRepository clienteRepository, PedidoRepository pedidoRepository) {
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
    }


    public ClienteResponseDTO criarCliente(ClienteDTO clienteDTO) {
        if (clienteCadastrado(clienteDTO.cpf())) {
            throw new ClienteCadastradoException("Cliente já cadastrado");
        }

        Cliente cliente = new Cliente(clienteDTO.nome(), clienteDTO.cpf());

        Cliente clienteSalvo = clienteRepository.save(cliente);

        return new ClienteResponseDTO(
                clienteSalvo.getId(),
                clienteSalvo.getNome(),
                clienteSalvo.getCpf(),
                List.of()
        );

    }
    public void deletarCliente(String cpf) {

        Cliente cliente = clienteRepository.findByCpf(cpf)
                .orElseThrow(() -> new ClienteCadastradoException("Cliente não encontrado"));

        List<Pedido> pedidosCliente = pedidoRepository.findByClienteCpf(cpf);

        if (!pedidosCliente.isEmpty()) {
            throw new ClienteComPedidoVinculadoException(
                    "O cliente possui pedidos vinculados, não sendo possível deletar o seu cadastro"
            );
        }

        clienteRepository.delete(cliente);
    }
    public List<ClienteResponseDTO> buscarTodosOsClientes() {
        List<Cliente> clienteList = clienteRepository.findAll();
        return clienteList.stream()
                .map(this::toResponseDTO)
                .toList();    }


    private boolean clienteCadastrado(String cpf) {
        return clienteRepository.findByCpf(cpf).isPresent();
    }
    private ClienteResponseDTO toResponseDTO(Cliente cliente) {
        List<Long> pedidoIds = cliente.getPedido()
                .stream()
                .map(Pedido::getId)
                .toList();

        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                pedidoIds
        ); 


}
}