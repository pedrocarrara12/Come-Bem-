package br.com.comebem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequestDTO(
        @NotBlank(message = "O nome e obrigatorio")
        @Size(min = 3, max = 120, message = "O nome deve ter entre 3 e 120 caracteres")
        String nome,

        @NotBlank(message = "O CPF e obrigatorio")
        @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 digitos")
        String cpf,

        @Pattern(regexp = "^$|[0-9()+\\-\\s]{8,20}$", message = "O telefone e invalido")
        String telefone,

        @Email(message = "O email e invalido")
        @Size(max = 160, message = "O email deve ter no maximo 160 caracteres")
        String email
) {
}
