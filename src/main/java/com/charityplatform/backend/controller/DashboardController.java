package com.charityplatform.backend.controller;

import com.charityplatform.backend.dto.AdminDashboardDTO;
import com.charityplatform.backend.dto.CharityDashboardDTO;
import com.charityplatform.backend.dto.DonorDashboardDTO;
import com.charityplatform.backend.model.User;
import com.charityplatform.backend.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/donor")
    @PreAuthorize("hasAuthority('ROLE_DONOR')")
    public ResponseEntity<DonorDashboardDTO> getDonorDashboard(@AuthenticationPrincipal User currentUser) {
        DonorDashboardDTO dashboardData = dashboardService.getDonorDashboardData(currentUser);
        return ResponseEntity.ok(dashboardData);
    }
    @GetMapping("/charity")
    @PreAuthorize("hasAuthority('ROLE_CHARITY_ADMIN')")
    public ResponseEntity<CharityDashboardDTO> getCharityDashboard(@AuthenticationPrincipal User currentUser) {
        CharityDashboardDTO dashboardData = dashboardService.getCharityDashboardData(currentUser);
        return ResponseEntity.ok(dashboardData);
    }
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_PLATFORM_ADMIN')")
    public ResponseEntity<AdminDashboardDTO> getAdminDashboard() {
        AdminDashboardDTO dashboardData = dashboardService.getAdminDashboardData();
        return ResponseEntity.ok(dashboardData);
    }
}