// src/main/java/com/charityplatform/backend/model/User.java
package com.charityplatform.backend.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Slf4j
@Entity
@Table(name="users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames="username"),
                @UniqueConstraint(columnNames = "email")
        })
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message="aint no way username gon be blank ")
    @Size(min=3,max=32,message="you got some weird name(username should be min 3 max 50 chars)")
    @Column(nullable = false,unique = true)
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message="Password blank? nuh")
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean emailVerified=false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role_name", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

    // --- THIS IS THE CORRECTED MAPPING ---
    // It is mapped by the field named "user" in the VerificationToken class.
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private VerificationToken verificationToken;
    // --- END OF CORRECTION ---

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charity_id") // No referencedColumnName needed, 'id' is default
    @JsonIgnore
    private Charity charity;

    public User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.emailVerified=false;
    }

    // --- ALL GETTERS AND SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    @Override
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    @Override
    public String getPassword() { return password; }
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
    public VerificationToken getVerificationToken() { return verificationToken; }
    public void setVerificationToken(VerificationToken verificationToken) { this.verificationToken = verificationToken; }
    public Charity getCharity() { return charity; }
    public void setCharity(Charity charity) { this.charity = charity; }

    // --- ALL UserDetails METHODS ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());
    }
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return this.emailVerified; }
}