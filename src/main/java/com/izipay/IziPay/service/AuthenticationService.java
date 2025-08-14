package com.izipay.IziPay.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.zxing.WriterException;
import com.izipay.IziPay.exceptions.UserExistsException;
import com.izipay.IziPay.model.Account;
import com.izipay.IziPay.model.LoginAttempt;
import com.izipay.IziPay.model.Token;
import com.izipay.IziPay.model.User;
import com.izipay.IziPay.model.dto.request.RegisterRequestDTO;
import com.izipay.IziPay.model.dto.response.AuthenticationResponse;
import com.izipay.IziPay.model.enums.AttemptStatus;
import com.izipay.IziPay.model.enums.UserState;
import com.izipay.IziPay.repository.AccountRepository;
import com.izipay.IziPay.repository.LoginAttemptRepository;
import com.izipay.IziPay.repository.TokenRepository;
import com.izipay.IziPay.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private QRCodeService qrCodeService;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    @Transactional
    public AuthenticationResponse register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserExistsException("Email already registered");
        }

        if (userRepository.existsByPhone(request.phone())) {
            throw new UserExistsException("Phone number already registered");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new UserExistsException("Username already taken");
        }

        User user = new User();
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPin(passwordEncoder.encode(request.pin()));
        user.setRole(request.role());
        user.setUserState(UserState.ATIVO);
        user.setCreatedAt(LocalDateTime.now());

        user = userRepository.save(user);

        try {
            Account account = createAccountForUser(user);
            accountRepository.save(account);
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Error generating QR Code", e);
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthenticationResponse(
                accessToken,
                refreshToken,
                "User registered successfully");
    }

    private Account createAccountForUser(User user) throws WriterException, IOException {
        Account account = new Account();
        account.setUser(user);

        String accountNumber = generateUniqueAccountNumber();
        account.setAccountNumber(accountNumber);
        account.setBalance(BigDecimal.ZERO);

        String qrCodeBase64 = qrCodeService.generateQRCodeBase64(accountNumber);
        account.setQrCodehash(qrCodeBase64);

        account.setCreatedAt(LocalDateTime.now());
        return account;
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        Random random = new Random();

        do {
            accountNumber = String.format("%010d", random.nextLong(1_000_000_0000L));
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    public AuthenticationResponse authenticate(User request, String ipAddress, String deviceInfo) {
    boolean success = false;
    User user = null;

    try {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        success = true; // Se passou pela autenticação sem exceção
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        revokeAllTokenByUser(user);
        saveUserToken(accessToken, refreshToken, user);

        return new AuthenticationResponse(accessToken, refreshToken, "User login was successful");

    } finally {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setUser(user);
        attempt.setStatus(success ? AttemptStatus.SUCCESS : AttemptStatus.FAILED);
        attempt.setIpAddress(ipAddress);
        attempt.setDeviceInfo(deviceInfo);
        attempt.setAttemptedAt(LocalDateTime.now());
        loginAttemptRepository.save(attempt);
    }
}


    private void revokeAllTokenByUser(User user) {
        List<Token> validTokens = tokenRepository.findAllAccessTokensByUser(user.getId());
        if (validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(t -> {
            t.setLoggedOut(true);
        });

        tokenRepository.saveAll(validTokens);
    }

    private void saveUserToken(String accessToken, String refreshToken, User user) {
        Token token = new Token();
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setLoggedOut(false);
        token.setUser(user);
        tokenRepository.save(token);
    }

    public ResponseEntity refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        String username = jwtService.extractUsername(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("No user found"));

        if (jwtService.isValidRefreshToken(token, user)) {
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            revokeAllTokenByUser(user);
            saveUserToken(accessToken, refreshToken, user);

            return new ResponseEntity(new AuthenticationResponse(accessToken, refreshToken, "New token generated"),
                    HttpStatus.OK);
        }

        return new ResponseEntity(HttpStatus.UNAUTHORIZED);

    }
}
