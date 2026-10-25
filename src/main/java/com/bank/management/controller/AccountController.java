package com.bank.management.controller;

import com.bank.management.service.AccountService;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/me")
    public Map<String, Object> myAccount(
            Authentication authentication) {

        return accountService.myAccount(authentication);
    }
}