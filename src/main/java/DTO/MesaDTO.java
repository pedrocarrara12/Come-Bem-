package DTO;

import jakarta.validation.constraints.Positive;

public record MesaDTO(
        @Positive(message = "A quantidade de lugares deve ser maior que zero")
        int lugares
) {
}
