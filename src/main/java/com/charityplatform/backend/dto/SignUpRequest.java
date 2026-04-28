package com.charityplatform.backend.dto;

import jakarta.validation.constraints.*;





public class SignUpRequest {
    @NotBlank(message = "give username atleast")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;



    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    public SignUpRequest(String email, String username, String password) {

        this.email = email;
        this.username = username;
        this.password = password;
    }public SignUpRequest() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
