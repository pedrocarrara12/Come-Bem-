package br.com.comebem.controller;

import br.com.comebem.dto.AtendimentoIaRequestDTO;
import br.com.comebem.dto.AtendimentoIaResponseDTO;
import br.com.comebem.service.AtendimentoIaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/atendimento")
public class AtendimentoIaController {

    private final AtendimentoIaService atendimentoIaService;

    public AtendimentoIaController(AtendimentoIaService atendimentoIaService) {
        this.atendimentoIaService = atendimentoIaService;
    }

    @PostMapping("/ia")
    public ResponseEntity<AtendimentoIaResponseDTO> responder(@Valid @RequestBody AtendimentoIaRequestDTO dto) {
        return ResponseEntity.ok(atendimentoIaService.responder(dto));
    }
}
