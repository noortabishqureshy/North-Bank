package com.bank.management.controller;

import com.bank.management.dto.ChangePasswordRequest;
import com.bank.management.dto.RegisterRequest;
import com.bank.management.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {

        return Map.of(
                "headerName", token.getHeaderName(),
                "parameterName", token.getParameterName(),
                "token", token.getToken());
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {

        return authService.currentUser(authentication);
    }

    @PostMapping("/change-password")
    public Map<String, String> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest servletRequest) {

        authService.changePassword(authentication, request);

        return Map.of(
                "message",
                "Password changed successfully");
    }
}