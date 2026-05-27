package br.com.comebem.dto;

import br.com.comebem.enums.StatusMesa;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusMesaDTO(
        @NotNull(message = "O status da mesa e obrigatorio")
        StatusMesa status
) {
}
