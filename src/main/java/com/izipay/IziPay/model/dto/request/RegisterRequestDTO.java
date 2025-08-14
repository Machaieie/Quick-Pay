package com.izipay.IziPay.model.dto.request;

import com.izipay.IziPay.model.enums.Role;

public record RegisterRequestDTO(
    String fullName,
    String phone,
    String email,
    String username,
    String password,
    String pin,
    Role role
) {
    
}
