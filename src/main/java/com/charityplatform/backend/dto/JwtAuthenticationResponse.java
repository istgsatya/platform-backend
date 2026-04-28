package com.charityplatform.backend.dto;


import java.util.List;
public class JwtAuthenticationResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private Long UserId;
    private String userName;
    private List<String> roles;

    public JwtAuthenticationResponse(String accessToken, List<String> roles, String userName, Long userId, String tokenType) {
        this.accessToken = accessToken;
        this.roles = roles;
        this.userName = userName;
        this.UserId = userId;
        this.tokenType = tokenType;
    }

    public Long getUserId() {
        return UserId;
    }

    public void setUserId(Long userId) {
        UserId = userId;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
