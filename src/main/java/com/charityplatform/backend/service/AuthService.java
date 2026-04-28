package com.charityplatform.backend.service;

import com.charityplatform.backend.dto.JwtAuthenticationResponse;
import com.charityplatform.backend.dto.LoginRequest;
import com.charityplatform.backend.dto.SignUpRequest;
import com.charityplatform.backend.dto.UserResponseDTO;
import com.charityplatform.backend.exception.UserAlreadyExistsException;
import com.charityplatform.backend.model.Role;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.model.VerificationToken;
import com.charityplatform.backend.repository.UserRepository;
import com.charityplatform.backend.repository.VerificationTokenRepository;
import com.charityplatform.backend.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final VerificationTokenRepository tokenRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider, VerificationTokenRepository tokenRepository ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.tokenRepository = tokenRepository;
    }

    // --- All of your existing methods are unchanged and still work ---

    @Transactional
    public User registerUser(SignUpRequest signUpRequest) {
        if(userRepository.existsByUsername(signUpRequest.getUsername())) { throw new UserAlreadyExistsException("Username "+ signUpRequest.getUsername() +" is already taken"); }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) { throw new UserAlreadyExistsException("Email " + signUpRequest.getEmail() + " is already in use!"); }
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_DONOR);
        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, savedUser);
        tokenRepository.save(verificationToken);
        String verificationUrl = "http://localhost:8080/api/auth/verify-account?token=" + token;
        logger.info("New verification token has been sent to " + verificationUrl);
        return savedUser;
    }

    public JwtAuthenticationResponse authenticateAndGetResponse(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsernameOrEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        User userDetails = (User) authentication.getPrincipal();
        return new JwtAuthenticationResponse(jwt, userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()), userDetails.getUsername(), userDetails.getId(), "Bearer");
    }

    @Transactional
    public void verifyAccount(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token).orElseThrow(() -> new IllegalStateException("Invalid verification token"));
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(verificationToken);
            throw new IllegalStateException("Expired verification token, please register again.");
        }
        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        tokenRepository.delete(verificationToken);
        logger.info("Account for user {} has been verified", user.getUsername());
    }

    // --- START: THE FINAL BULLETPROOF METHOD ---
    /**
     * Safely fetches the current user and converts it to a DTO to avoid LazyInitializationExceptions.
     * This explicitly re-fetches the user from the database within a new transaction.
     * @param detachedCurrentUser The User object from the security context, which may be detached.
     * @return A safe, fully-populated UserResponseDTO.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getSafeCurrentUser(User detachedCurrentUser) {
        // The key is to re-fetch the user by their ID. This guarantees a session-attached, "managed" entity.
        User managedUser = userRepository.findById(detachedCurrentUser.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database."));
        // Now, we call the fromEntity method while the session is still active.
        // This will trigger all the lazy-loading proxies (wallets, charity, etc.) safely.
        return UserResponseDTO.fromEntity(managedUser);
    }
    // --- END: THE FINAL BULLETPROOF METHOD ---
}