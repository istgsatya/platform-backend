package com.charityplatform.backend.controller;

import com.charityplatform.backend.dto.*;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        authService.registerUser(signUpRequest);
        return ResponseEntity.ok(new MessageResponse(true, "User registered successfully! Check your email to verify your account."));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtAuthenticationResponse response = authService.authenticateAndGetResponse(loginRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify-account")
    public ResponseEntity<?> verifyAccount(@RequestParam("token") String token) {
        try {
            authService.verifyAccount(token);
            return ResponseEntity.ok(new MessageResponse(true, "Account verified successfully!"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(false, e.getMessage()));
        }
    }

    // --- START: THE FINAL, CORRECTED /me ENDPOINT ---
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        // This now calls our new, safe service method which re-fetches the user
        // and converts it to a DTO, permanently solving the LazyInitializationException.
        UserResponseDTO safeUserData = authService.getSafeCurrentUser(currentUser);
        return ResponseEntity.ok(safeUserData);
    }
    // --- END: THE FINAL, CORRECTED /me ENDPOINT ---
}