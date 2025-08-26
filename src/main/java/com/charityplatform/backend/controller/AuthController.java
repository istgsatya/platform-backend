package com.charityplatform.backend.controller;



import com.charityplatform.backend.dto.JwtAuthenticationResponse;
import com.charityplatform.backend.dto.LoginRequest;
import com.charityplatform.backend.dto.MessageResponse;
import com.charityplatform.backend.dto.SignUpRequest;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.service.AuthService;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.Valid; // For validating the request body
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


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
    return ResponseEntity.ok(new MessageResponse(true,"User registered successfully! Check your email to verify your account."));
}
@PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    String jwt= authService.authenticateUser(loginRequest);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User userDetails =(User) authentication.getPrincipal();
    return ResponseEntity.ok(new JwtAuthenticationResponse(
            jwt,
            userDetails.getAuthorities().stream()
                    .map(item->item.getAuthority())
                    .collect(Collectors.toList()),

            userDetails.getUsername(),
            userDetails.getId(),
            "Bearer"

    ));
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


}
