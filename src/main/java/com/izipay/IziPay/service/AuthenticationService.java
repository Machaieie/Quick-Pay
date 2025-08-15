package com.izipay.IziPay.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.zxing.WriterException;
import com.izipay.IziPay.aspect.LogAction;
import com.izipay.IziPay.exceptions.InvalidCredentialsException;
import com.izipay.IziPay.exceptions.UserBlockedException;
import com.izipay.IziPay.exceptions.UserExistsException;
import com.izipay.IziPay.exceptions.UserNotFoundException;
import com.izipay.IziPay.model.Account;
import com.izipay.IziPay.model.LoginAttempt;
import com.izipay.IziPay.model.SystemLog;
import com.izipay.IziPay.model.Token;
import com.izipay.IziPay.model.User;
import com.izipay.IziPay.model.dto.request.LoginRequestDTO;
import com.izipay.IziPay.model.dto.request.RegisterRequestDTO;
import com.izipay.IziPay.model.dto.response.AuthenticationResponse;
import com.izipay.IziPay.model.enums.AttemptStatus;
import com.izipay.IziPay.model.enums.SystemAction;
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
    private EmailService emailService;

    @Autowired
    private QRCodeService qrCodeService;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    @LogAction(action = SystemAction.REGISTER, details = "Usuário registrado com sucesso")
    @Transactional
    public AuthenticationResponse register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserExistsException("Email already registered");
        }

        if (userRepository.existsByPhone(request.phone())) {
            throw new UserExistsException("Phone number already registered");
        }

        String accountNumber = generateUniqueAccountNumber();
        String pin = generatePin();
        User user = new User();
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        user.setUsername(request.phone());
        user.setPassword(passwordEncoder.encode(pin));
        user.setPin(passwordEncoder.encode(pin));
        user.setRole(request.role());
        user.setUserState(UserState.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());

        user = userRepository.save(user);

        Account account;
        try {
            account = createAccountForUser(user, accountNumber);
            accountRepository.save(account);
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Error generating QR Code", e);
        }

        String emailBody = """
                <p>Olá %s,</p>
                <p>Seja bem-vindo à IziPay! Seus dados de login foram criados com sucesso.</p>
                <ul>
                    <li><b>Titular:</b> %s</li>
                    <li><b>Número da Conta:</b> %s</li>
                    <li><b>NIB:</b> %s</li>
                    <li><b>PIN:</b> %s</li>
                </ul>
                <p>Use esses dados para acessar seu perfil no aplicativo. Recomendamos alterar seu PIN após o primeiro login.</p>
                """
                .formatted(user.getFullName(), user.getFullName(), account.getAccountNumber(), account.getNib(), pin);

        emailService.send(user.getEmail(), "Bem-vindo à IziPay - Seus dados de login", emailBody);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthenticationResponse(
                accessToken,
                refreshToken,
                "User registered successfully");
    }

    private Account createAccountForUser(User user, String userAccount) throws WriterException, IOException {
        Account account = new Account();
        account.setUser(user);

        account.setAccountNumber(userAccount);
        account.setBalance(BigDecimal.ZERO);
        account.setNib(generateUniqueNIB());
        String qrCodeBase64 = qrCodeService.generateQRCodeBase64(userAccount);
        account.setQrCodehash(qrCodeBase64);
        account.setQrCodeImage(qrCodeBase64);
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

    @LogAction(action = SystemAction.LOGIN_SUCCESS, details = "Login bem-sucedido")
    public AuthenticationResponse authenticate(LoginRequestDTO loginRequestDTO, HttpServletRequest httpRequest) {
        boolean success = false;

        User user = userRepository.findByUsername(loginRequestDTO.phone())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.isBlocked()) {
            throw new UserBlockedException("Account is blocked due to multiple failed login attempts");
        }

        String ipAddress = httpRequest.getRemoteAddr();
        String deviceInfo = httpRequest.getHeader("User-Agent");

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDTO.phone(), loginRequestDTO.password()));

            user.setFailedAttempts(0);
            userRepository.save(user);

            success = true;

            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            revokeAllTokenByUser(user);
            saveUserToken(accessToken, refreshToken, user);

            return new AuthenticationResponse(accessToken, refreshToken, "User login successful");

        } catch (BadCredentialsException ex) {
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);

            if (attempts >= 3) {
                user.setUserState(UserState.BLOCKED);
            }

            userRepository.save(user);
            throw new InvalidCredentialsException("Invalid username or password");
        } finally {
            // registra tentativas de login
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

    @LogAction(action = SystemAction.PIN_RESET, details = "PIN redefinido")
    @Transactional
    public String resetPinByEmail(String phone) {
        Optional<User> userOpt = userRepository.findByphone(phone);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("telefone não encontrado");
        }

        User user = userOpt.get();
        String newPin = generatePin();
        user.setPin(passwordEncoder.encode(newPin));
        userRepository.save(user);

        String emailBody = """
                    <p>Seu PIN foi redefinido com sucesso.</p>
                    <p><b>Novo PIN:</b> %s</p>
                    <p>Use-o para fazer login e altere-o depois.</p>
                """.formatted(newPin);

        emailService.send(user.getEmail(), "Redefinição de PIN", emailBody);

        return "Novo PIN enviado para o e-mail";
    }

    private String generatePin() {
        SecureRandom random = new SecureRandom();
        int pin = random.nextInt(9000) + 1000;
        return String.valueOf(pin);
    }

    private String generateUniqueNIB() {
        String nib;
        Random random = new Random();

        do {
            nib = String.format("%021d", Math.abs(random.nextLong()) % 1_000_000_000_000_000_000L);
        } while (accountRepository.existsByNib(nib));

        return nib;
    }

}
