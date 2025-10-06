package com.charityplatform.backend.dto;



import com.charityplatform.backend.model.Charity;


public class CharityResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String adminUsername;


    public CharityResponseDTO(Long id, String name, String description, String adminUsername) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.adminUsername = adminUsername;
    }
    public static CharityResponseDTO fromCharity(Charity charity){
        String username =(charity.getAdminUser()!=null)?charity.getAdminUser().getUsername():"N/A";
        return new CharityResponseDTO(charity.getId(), charity.getName(), charity.getDescription(), username) ;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }
}
