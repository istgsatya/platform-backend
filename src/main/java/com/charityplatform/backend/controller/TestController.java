package com.charityplatform.backend.controller;

import com.charityplatform.backend.contracts.PlatformLedger;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final PlatformLedger platformLedger;
    @Autowired
    public TestController(PlatformLedger platformLedger) {
        this.platformLedger = platformLedger;
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
    @GetMapping("/contract-owner")
    @PreAuthorize("hasAuthority('ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<String> getContractOwner() {
        try {
            // Call a read-only function on the smart contract
            String ownerAddress = platformLedger.platformOwner().send();
            return ResponseEntity.ok("Smart contract owner address is: " + ownerAddress);
        } catch (Exception e) {
            // If the call fails, this will give us a detailed error
            return ResponseEntity.internalServerError().body("Failed to call contract: " + e.getMessage());
        }
    }
}
