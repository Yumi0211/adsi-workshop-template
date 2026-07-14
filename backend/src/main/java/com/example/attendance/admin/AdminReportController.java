package com.example.attendance.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.attendance.admin.dto.LeaveObligationResponse;
import com.example.attendance.admin.dto.OvertimeReportResponse;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminReportController {

    private final AdminReportService adminReportService;

    public AdminReportController(AdminReportService adminReportService) {
        this.adminReportService = adminReportService;
    }

    @GetMapping("/overtime")
    public ResponseEntity<OvertimeReportResponse> getOvertimeReport(
            @RequestParam @Min(1900) @Max(2100) int year,
            @RequestParam @Min(1) @Max(12) int month) {
        return ResponseEntity.ok(adminReportService.getOvertimeReport(year, month));
    }

    @GetMapping("/leave-obligation")
    public ResponseEntity<LeaveObligationResponse> getLeaveObligationReport(
            @RequestParam @Min(1900) @Max(2100) int fiscalYear) {
        return ResponseEntity.ok(adminReportService.getLeaveObligationReport(fiscalYear));
    }
}
