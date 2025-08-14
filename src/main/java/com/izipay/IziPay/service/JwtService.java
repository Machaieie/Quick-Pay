package com.izipay.IziPay.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.izipay.IziPay.model.User;

import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    private final String SECRECT_KEY = "L7KKytHmAdCo6OTGykv6rMnD9T/+J5QHBp55IAQztI8=";

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isValid(String token, UserDetails usuario) {
        String username = extractUsername(token);
        return username.equals(usuario.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigninKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateToken(User usuario) {
        return Jwts.builder()
                .subject(usuario.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24 horas
                .signWith(getSigninKey())
                .compact();
    }

    public String generateAccessToken(User usuario) {
        return Jwts.builder()
                .subject(usuario.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + (15 * 60 * 1000))) // 15 minutos
                .signWith(getSigninKey())
                .compact();
    }

    public String generateRefreshToken(User usuario) {
        return Jwts.builder()
                .subject(usuario.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000))) 
                .signWith(getSigninKey())
                .compact();
    }

    public boolean isValidRefreshToken(String token, User usuario) {
        String username = extractUsername(token);
        return username.equals(usuario.getUsername()) && !isTokenExpired(token);
    }

    private SecretKey getSigninKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRECT_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
