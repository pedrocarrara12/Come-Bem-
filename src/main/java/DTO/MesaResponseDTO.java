package DTO;

import java.util.List;

public record MesaResponseDTO(
        Long id,
        int lugares,
        List<Long> pedidoIds
) {
}
