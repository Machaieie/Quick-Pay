package com.izipay.IziPay.model.dto.request;

import com.izipay.IziPay.model.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


public record RegisterRequestDTO(

    @NotBlank(message = "O nome completo não pode estar vazio")
    String fullName,

    @NotBlank(message = "O número de telefone não pode estar vazio")
    @Pattern(
        regexp = "\\+258(8[726345])\\d{6}$",
        message = "O número de telefone deve estar no formato +2588X... com prefixos válidos: 87, 86, 82, 83, 84 ou 85"
    )
    String phone,

    @NotBlank(message = "O email não pode estar vazio")
    @Email(message = "Email inválido")
    String email,

    @NotNull(message = "O papel (role) é obrigatório")
    Role role
) {}
