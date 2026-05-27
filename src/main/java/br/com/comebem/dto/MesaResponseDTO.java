package br.com.comebem.dto;

import br.com.comebem.enums.StatusMesa;

public record MesaResponseDTO(
        Long id,
        Integer numero,
        Integer capacidade,
        StatusMesa status
) {
}
