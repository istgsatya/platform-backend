package com.charityplatform.backend.service;

import com.charityplatform.backend.dto.SignUpRequest;
import com.charityplatform.backend.exception.UserAlreadyExistsException; // We'll create this custom exception
import com.charityplatform.backend.model.Role;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.repository.UserRepository;
import org.hibernate.annotations.NaturalId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import com.charityplatform.backend.dto.LoginRequest;
import com.charityplatform.backend.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import com.charityplatform.backend.model.VerificationToken;
import com.charityplatform.backend.repository.VerificationTokenRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final VerificationTokenRepository tokenRepository;
    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,AuthenticationManager authenticationManager,JwtTokenProvider tokenProvider,VerificationTokenRepository tokenRepository )
    {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.tokenRepository = tokenRepository;
    }


    @Transactional
    public User registerUser(SignUpRequest signUpRequest) {
        if(userRepository.existsByUsername(signUpRequest.getUsername()))
        {
            throw new UserAlreadyExistsException("Username "+ signUpRequest.getUsername() +" is already taken");


        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email " + signUpRequest.getEmail() + " is already in use!");
        }
        User user=new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());


        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        Set<Role> roles=new HashSet<>();
        roles.add(Role.ROLE_DONOR);
        user.setRoles(roles);

        User savedUser=userRepository.save(user);
        String token=UUID.randomUUID().toString();
        VerificationToken verificationToken=new VerificationToken(token,savedUser);
        tokenRepository.save(verificationToken);

        String VerificationUrl="http://localhost:8080/api/auth/verify-account?token=" + token;
        logger.info("New verification token has been sent to "+VerificationUrl);

        return savedUser;
//        return userRepository.save(user);
    }
    public String authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        return jwt;

    }
    @Transactional
    public void verifyAccount(String token) {
        VerificationToken verificationToken=tokenRepository.findByToken(token).orElseThrow(()->new IllegalStateException("invalid verification token"));

        if(verificationToken.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new IllegalStateException("expired verification token,register again ");

        }
        User user= verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        tokenRepository.delete(verificationToken);
        logger.info("Accoun for uesr {} has been verified", user.getUsername());

    }


    }
