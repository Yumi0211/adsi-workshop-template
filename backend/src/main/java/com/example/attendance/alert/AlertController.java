package com.example.attendance.alert;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.attendance.alert.dto.AlertPageResponse;
import com.example.attendance.alert.dto.AlertResponse;
import com.example.attendance.employee.EmployeeService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1")
public class AlertController {

    private final AlertService alertService;
    private final EmployeeService employeeService;

    public AlertController(AlertService alertService, EmployeeService employeeService) {
        this.alertService = alertService;
        this.employeeService = employeeService;
    }

    @GetMapping("/alerts/my")
    public ResponseEntity<List<AlertResponse>> getMyAlerts(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long employeeId = employeeService.findIdByEmail(userDetails.getUsername());
        var alerts = alertService.getMyAlerts(employeeId);
        return ResponseEntity.ok(alerts);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/alerts")
    public ResponseEntity<AlertPageResponse> getAllAlerts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        var response = alertService.getAllAlerts(page, size);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/alerts/{id}/acknowledge")
    public ResponseEntity<AlertResponse> acknowledge(@PathVariable @Min(1) Long id) {
        var response = alertService.acknowledge(id);
        return ResponseEntity.ok(response);
    }
}
