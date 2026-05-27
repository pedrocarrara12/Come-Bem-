package br.com.comebem.dto;

import java.util.List;

public record ClienteResponseDTO(
        Long id,
        String nome,
        String cpf,
        String telefone,
        String email,
        List<Long> pedidoIds
) {
}
