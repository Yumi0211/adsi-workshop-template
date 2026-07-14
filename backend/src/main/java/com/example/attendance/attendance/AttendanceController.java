package com.example.attendance.attendance;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.attendance.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.attendance.dto.MonthlySummaryResponse;
import com.example.attendance.employee.EmployeeService;

@RestController
@RequestMapping("/api/v1")
public class AttendanceController {

    private final MonthlyReportService monthlyReportService;
    private final EmployeeService employeeService;

    public AttendanceController(MonthlyReportService monthlyReportService,
                                EmployeeService employeeService) {
        this.monthlyReportService = monthlyReportService;
        this.employeeService = employeeService;
    }

    @GetMapping("/attendances/monthly")
    public ResponseEntity<MonthlyAttendanceResponse> getMonthlyAttendance(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month) {
        Long employeeId = employeeService.findIdByEmail(userDetails.getUsername());
        var response = monthlyReportService.getMonthlyAttendance(employeeId, year, month);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/attendances/monthly/summary")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month) {
        Long employeeId = employeeService.findIdByEmail(userDetails.getUsername());
        var response = monthlyReportService.getMonthlySummary(employeeId, year, month);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('APPROVER', 'ADMIN')")
    @GetMapping("/departments/{departmentId}/attendances/monthly")
    public ResponseEntity<List<MonthlyAttendanceResponse>> getDepartmentMonthlyAttendance(
            @PathVariable Long departmentId,
            @RequestParam int year,
            @RequestParam int month) {
        var response = monthlyReportService.getDepartmentMonthlyAttendance(departmentId, year, month);
        return ResponseEntity.ok(response);
    }
}
