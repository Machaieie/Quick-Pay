package com.izipay.IziPay.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.izipay.IziPay.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByphone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByUsername(String username);

   

}
