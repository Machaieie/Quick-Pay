package com.izipay.IziPay.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.izipay.IziPay.model.Token;

public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("""
            select t from Token t inner join User u on t.user.id = u.id
            where t.user.id = :userId and t.loggedOut = false
            """)
    List<Token> findAllAccessTokensByUser(Integer userId);

    Optional<Token> findByAccessToken(String token);

    Optional<Token> findByRefreshToken(String token);

    List<Token> findAllAccessTokensByUser(Long id);
}
