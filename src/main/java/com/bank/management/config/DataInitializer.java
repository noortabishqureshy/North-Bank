package com.bank.management.config;

import com.bank.management.entity.User;
import com.bank.management.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,

            @Value("${bank.admin.username}") String username,
            @Value("${bank.admin.password}") String password,
            @Value("${bank.admin.email}") String email) {

        return args -> {

            if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new IllegalStateException(
                        "Admin password exceeds the BCrypt input limit");
            }

            if (userRepository.findByUsername(username).isPresent()) {
                return;
            }

            if (userRepository.existsByEmail(email)) {
                throw new IllegalStateException(
                        "Admin email is already associated with another account");
            }

            User admin = new User(
                    username,
                    passwordEncoder.encode(password),
                    "System Administrator",
                    email,
                    "ADMIN");

            userRepository.save(admin);

            System.out.println(
                    "Initial administrator created: " + username);
        };
    }
}