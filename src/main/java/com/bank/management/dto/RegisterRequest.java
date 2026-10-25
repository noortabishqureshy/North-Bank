package com.bank.management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank @Size(min = 4, max = 50) @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username contains invalid characters") String username,

        @NotBlank @Size(min = 12, max = 72) String password,

        @NotBlank @Size(min = 2, max = 100) String fullName,

        @NotBlank @Email @Size(max = 150) String email) {
}