package br.com.comebem.controller;

import br.com.comebem.dto.AtualizarStatusPedidoDTO;
import br.com.comebem.dto.ItemPedidoRequestDTO;
import br.com.comebem.dto.PedidoRequestDTO;
import br.com.comebem.dto.PedidoResponseDTO;
import br.com.comebem.enums.StatusPedido;
import br.com.comebem.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criar(@Valid @RequestBody PedidoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.criar(dto));
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listar() {
        return ResponseEntity.ok(pedidoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PedidoResponseDTO>> listarPorStatus(@PathVariable StatusPedido status) {
        return ResponseEntity.ok(pedidoService.listarPorStatus(status));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PedidoResponseDTO>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(pedidoService.listarPorCliente(clienteId));
    }

    @GetMapping("/mesa/{mesaId}")
    public ResponseEntity<List<PedidoResponseDTO>> listarPorMesa(@PathVariable Long mesaId) {
        return ResponseEntity.ok(pedidoService.listarPorMesa(mesaId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PedidoResponseDTO> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarStatusPedidoDTO dto
    ) {
        return ResponseEntity.ok(pedidoService.atualizarStatus(id, dto.status()));
    }

    @PostMapping("/{id}/itens")
    public ResponseEntity<PedidoResponseDTO> adicionarItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemPedidoRequestDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.adicionarItem(id, dto));
    }

    @DeleteMapping("/{id}/itens/{itemId}")
    public ResponseEntity<Void> removerItem(@PathVariable Long id, @PathVariable Long itemId) {
        pedidoService.removerItem(id, itemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.cancelar(id));
    }

    @PostMapping("/{id}/finalizar")
    public ResponseEntity<PedidoResponseDTO> finalizar(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.finalizar(id));
    }
}
