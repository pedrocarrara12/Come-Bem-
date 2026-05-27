package br.com.comebem.controller;

import br.com.comebem.dto.AtualizarStatusMesaDTO;
import br.com.comebem.dto.MesaRequestDTO;
import br.com.comebem.dto.MesaResponseDTO;
import br.com.comebem.enums.StatusMesa;
import br.com.comebem.service.MesaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mesas")
public class MesaController {

    private final MesaService mesaService;

    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    @PostMapping
    public ResponseEntity<MesaResponseDTO> criar(@Valid @RequestBody MesaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mesaService.criar(dto));
    }

    @GetMapping
    public ResponseEntity<List<MesaResponseDTO>> listar() {
        return ResponseEntity.ok(mesaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MesaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(mesaService.buscarPorId(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<MesaResponseDTO>> listarPorStatus(@PathVariable StatusMesa status) {
        return ResponseEntity.ok(mesaService.listarPorStatus(status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MesaResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MesaRequestDTO dto
    ) {
        return ResponseEntity.ok(mesaService.atualizar(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MesaResponseDTO> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarStatusMesaDTO dto
    ) {
        return ResponseEntity.ok(mesaService.atualizarStatus(id, dto.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        mesaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
