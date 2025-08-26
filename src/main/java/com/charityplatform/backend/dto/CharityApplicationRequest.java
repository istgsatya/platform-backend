package com.charityplatform.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;




public class CharityApplicationRequest {


    @NotBlank(message = "Charity name is required.")
    @Size(max = 255, message = "Charity name cannot exceed 255 characters.")
    private String name;




    @NotBlank(message = "Description is required.")
    @Size(min = 50, message = "Description must be at least 50 characters long.")
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
