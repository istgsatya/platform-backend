package com.charityplatform.backend.dto;

import com.charityplatform.backend.model.User;
import com.charityplatform.backend.model.Wallet;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private Set<String> roles;
    private int trustPoints;
    private int vipLevel;
    private List<String> wallets; // Just a list of addresses
    private Long charityId; // Null if not a charity admin

    public static UserResponseDTO fromEntity(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setTrustPoints(user.getTrustPoints());
        dto.setVipLevel(user.getVipLevel());

        // Safely extract role names
        dto.setRoles(user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet()));

        // Safely extract wallet addresses
        dto.setWallets(user.getWallets().stream()
                .map(Wallet::getAddress)
                .collect(Collectors.toList()));

        // Safely get charity ID
        if (user.getCharity() != null) {
            dto.setCharityId(user.getCharity().getId());
        }

        return dto;
    }

    // --- Add all Getters and Setters Below ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    public int getTrustPoints() { return trustPoints; }
    public void setTrustPoints(int trustPoints) { this.trustPoints = trustPoints; }
    public int getVipLevel() { return vipLevel; }
    public void setVipLevel(int vipLevel) { this.vipLevel = vipLevel; }
    public List<String> getWallets() { return wallets; }
    public void setWallets(List<String> wallets) { this.wallets = wallets; }
    public Long getCharityId() { return charityId; }
    public void setCharityId(Long charityId) { this.charityId = charityId; }
}