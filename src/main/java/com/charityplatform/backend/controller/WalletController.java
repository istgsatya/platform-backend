package com.charityplatform.backend.controller;

import com.charityplatform.backend.dto.MessageResponse;
import com.charityplatform.backend.dto.WalletRegistrationRequest;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallets")
@PreAuthorize("isAuthenticated()") // Secure the whole controller
public class WalletController {

    private final WalletService walletService;

    @Autowired
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerWallet(@Valid @RequestBody WalletRegistrationRequest request, @AuthenticationPrincipal User currentUser) {
        try {
            walletService.registerWallet(request.getAddress(), currentUser);
            return ResponseEntity.ok(new MessageResponse(true, "Wallet registered successfully."));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(false, e.getMessage()));
        }
    }
}