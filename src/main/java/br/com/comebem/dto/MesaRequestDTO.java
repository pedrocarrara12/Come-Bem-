package br.com.comebem.dto;

import br.com.comebem.enums.StatusMesa;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MesaRequestDTO(
        @NotNull(message = "O numero da mesa e obrigatorio")
        @Positive(message = "O numero da mesa deve ser positivo")
        Integer numero,

        @NotNull(message = "A capacidade e obrigatoria")
        @Positive(message = "A capacidade deve ser positiva")
        Integer capacidade,

        StatusMesa status
) {
}
