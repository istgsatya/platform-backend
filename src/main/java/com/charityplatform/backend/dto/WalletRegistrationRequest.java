package com.charityplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public class WalletRegistrationRequest {

    @NotBlank(message = "Address cannot be blank")
    @Length(min = 42, max = 42, message = "Address must be 42 characters long")
    private String address;

    // Getter and Setter
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}