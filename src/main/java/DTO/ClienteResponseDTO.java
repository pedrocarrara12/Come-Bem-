package DTO;

import java.util.List;

public record ClienteResponseDTO(
        Long id,
        String nome,
        String cpf,
        List<Long> pedidoIds
) {
}
