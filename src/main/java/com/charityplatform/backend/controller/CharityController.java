package com.charityplatform.backend.controller;



import com.charityplatform.backend.dto.CharityApplicationRequest;
import com.charityplatform.backend.dto.MessageResponse;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.service.CharityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper; // Add this import
import java.io.IOException;

@RestController
@RequestMapping("/api/charities")
public class CharityController {


        private final CharityService charityService;
        @Autowired
        public CharityController(CharityService charityService) {
            this.charityService = charityService;

        }
        @PostMapping(value= "/apply",consumes = {"multipart/form-data"})
        @PreAuthorize("hasAuthority('ROLE_DONOR')")
        public ResponseEntity<?> applyForCharityVerification(

            @RequestPart("application") CharityApplicationRequest request,
            @RequestPart("document") MultipartFile document,
            @AuthenticationPrincipal User currentUser)

        {
            charityService.applyForVerification(request,document,currentUser);
            return ResponseEntity.ok(new MessageResponse(true,"Charity application submitted successfully. pls wait for admin review"));

    }



}
