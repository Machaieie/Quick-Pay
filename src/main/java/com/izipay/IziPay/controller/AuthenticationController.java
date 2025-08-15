package com.izipay.IziPay.controller;

import com.izipay.IziPay.model.User;
import com.izipay.IziPay.model.dto.request.LoginRequestDTO;
import com.izipay.IziPay.model.dto.request.RegisterRequestDTO;
import com.izipay.IziPay.model.dto.response.AuthenticationResponse;
import com.izipay.IziPay.service.AuthenticationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequestDTO request) throws IOException {
        AuthenticationResponse response = authenticationService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody LoginRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        AuthenticationResponse response = authenticationService.authenticate(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        return authenticationService.refreshToken(request, response);
    }

    @PostMapping("/reset-pin/{phone}")
    public ResponseEntity<String> resetPin(@PathVariable String phone) {
        String result = authenticationService.resetPinByEmail(phone);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Token não informado");
        }
        return ResponseEntity.ok("Logout realizado com sucesso");
    }
}
