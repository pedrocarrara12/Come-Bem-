package br.com.comebem;

import br.com.comebem.dto.ClienteRequestDTO;
import br.com.comebem.dto.ItemPedidoRequestDTO;
import br.com.comebem.dto.MesaRequestDTO;
import br.com.comebem.dto.PedidoRequestDTO;
import br.com.comebem.dto.PedidoResponseDTO;
import br.com.comebem.dto.ProdutoRequestDTO;
import br.com.comebem.enums.CategoriaProduto;
import br.com.comebem.enums.StatusMesa;
import br.com.comebem.enums.StatusPedido;
import br.com.comebem.exception.ClienteJaCadastradoException;
import br.com.comebem.exception.ProdutoInativoException;
import br.com.comebem.exception.RegraDeNegocioException;
import br.com.comebem.service.ClienteService;
import br.com.comebem.service.MesaService;
import br.com.comebem.service.PedidoService;
import br.com.comebem.service.ProdutoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ComeBemApplicationTests {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private MesaService mesaService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private PedidoService pedidoService;

    @Test
    void deveCadastrarClienteEImpedirCpfDuplicado() {
        ClienteRequestDTO cliente = new ClienteRequestDTO(
                "Maria Silva",
                "12345678901",
                "65999990000",
                "maria@exemplo.com"
        );

        clienteService.criar(cliente);

        assertThatThrownBy(() -> clienteService.criar(cliente))
                .isInstanceOf(ClienteJaCadastradoException.class);
    }

    @Test
    void deveCriarPedidoCalculandoTotalEOcupandoMesa() {
        Long clienteId = criarCliente("12345678902");
        Long mesaId = criarMesa(10);
        Long produtoId = criarProduto("Prato", "35.50", true);

        PedidoResponseDTO pedido = pedidoService.criar(new PedidoRequestDTO(
                clienteId,
                mesaId,
                List.of(new ItemPedidoRequestDTO(produtoId, 2))
        ));

        assertThat(pedido.valorTotal()).isEqualByComparingTo("71.00");
        assertThat(pedido.status()).isEqualTo(StatusPedido.ABERTO);
        assertThat(mesaService.buscarPorId(mesaId).status()).isEqualTo(StatusMesa.OCUPADA);
    }

    @Test
    void naoDeveAdicionarProdutoInativoAoPedido() {
        Long clienteId = criarCliente("12345678903");
        Long mesaId = criarMesa(11);
        Long produtoId = criarProduto("Bebida", "8.00", false);

        assertThatThrownBy(() -> pedidoService.criar(new PedidoRequestDTO(
                clienteId,
                mesaId,
                List.of(new ItemPedidoRequestDTO(produtoId, 1))
        ))).isInstanceOf(ProdutoInativoException.class);
    }

    @Test
    void deveRespeitarFluxoDeStatusEFinalizarPedido() {
        PedidoResponseDTO pedido = criarPedidoCompleto("12345678904", 12);

        pedidoService.atualizarStatus(pedido.id(), StatusPedido.EM_PREPARO);
        pedidoService.atualizarStatus(pedido.id(), StatusPedido.PRONTO);
        pedidoService.atualizarStatus(pedido.id(), StatusPedido.ENTREGUE);
        PedidoResponseDTO finalizado = pedidoService.finalizar(pedido.id());

        assertThat(finalizado.status()).isEqualTo(StatusPedido.FINALIZADO);
        assertThat(mesaService.buscarPorId(finalizado.mesaId()).status()).isEqualTo(StatusMesa.LIVRE);
        assertThatThrownBy(() -> pedidoService.adicionarItem(
                pedido.id(),
                new ItemPedidoRequestDTO(finalizado.itens().get(0).produtoId(), 1)
        )).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void deveCancelarPedidoAbertoELiberarMesa() {
        PedidoResponseDTO pedido = criarPedidoCompleto("12345678905", 13);

        PedidoResponseDTO cancelado = pedidoService.cancelar(pedido.id());

        assertThat(cancelado.status()).isEqualTo(StatusPedido.CANCELADO);
        assertThat(mesaService.buscarPorId(pedido.mesaId()).status()).isEqualTo(StatusMesa.LIVRE);
    }

    private Long criarCliente(String cpf) {
        return clienteService.criar(new ClienteRequestDTO("Cliente Teste", cpf, null, null)).id();
    }

    private Long criarMesa(int numero) {
        return mesaService.criar(new MesaRequestDTO(numero, 4, null)).id();
    }

    private Long criarProduto(String nome, String preco, boolean ativo) {
        return produtoService.criar(new ProdutoRequestDTO(
                nome,
                "Descricao",
                new BigDecimal(preco),
                CategoriaProduto.PRATO_PRINCIPAL,
                ativo
        )).id();
    }

    private PedidoResponseDTO criarPedidoCompleto(String cpf, int numeroMesa) {
        Long clienteId = criarCliente(cpf);
        Long mesaId = criarMesa(numeroMesa);
        Long produtoId = criarProduto("Produto " + numeroMesa, "10.00", true);
        return pedidoService.criar(new PedidoRequestDTO(
                clienteId,
                mesaId,
                List.of(new ItemPedidoRequestDTO(produtoId, 1))
        ));
    }
}
