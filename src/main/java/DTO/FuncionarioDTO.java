package DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record FuncionarioDTO(
        @NotBlank(message = "O nome e obrigatorio")
        String nome,

        @NotBlank(message = "O CPF e obrigatorio")
        @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 digitos")
        String cpf
) {
}
