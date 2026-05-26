package DTO;

import java.util.List;

public record FuncionarioResponseDTO(
        Long id,
        String nome,
        String cpf,
        List<Long> pedidoIds
) {
}
