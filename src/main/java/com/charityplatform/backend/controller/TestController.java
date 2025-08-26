package com.charityplatform.backend.controller;


 import org.springframework.http.ResponseEntity;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.web.bind.annotation.RestController;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/test")
public class TestController {
    @GetMapping("/all")
    public ResponseEntity<String> allAccess() {
        return ResponseEntity.ok("this content is for any authenticated user.");
    }



    @GetMapping("/donor")
    @PreAuthorize("hasAuthority('ROLE_DONOR')")
    public ResponseEntity<String> donorAccess() {
        return ResponseEntity.ok("Success! you have access to Donor specific content");
    }
    @GetMapping("/charity")
    @PreAuthorize("hasAuthority('ROLE_CHARITY_ADMIN')")
    public ResponseEntity<String> charityAdminAccess() {
        return ResponseEntity.ok("Success! you have access to Charity admin specific content");
    }
    @GetMapping("/platform-admin")
    @PreAuthorize("hasAuthority('ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<String> platformAdminAccess() {
        return ResponseEntity.ok("Success! you have access to Platform Admin specific content");
    }
}
