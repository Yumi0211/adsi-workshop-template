package com.example.attendance.leave;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.attendance.leave.dto.LeaveBalanceResponse;
import com.example.attendance.leave.dto.LeaveRequestCreateRequest;
import com.example.attendance.leave.dto.LeaveRequestResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping("/balance")
    public ResponseEntity<LeaveBalanceResponse> getBalance(
            @RequestHeader("X-Employee-Id") Long employeeId) {
        return ResponseEntity.ok(leaveService.getBalance(employeeId));
    }

    @PostMapping("/requests")
    public ResponseEntity<LeaveRequestResponse> createRequest(
            @RequestHeader("X-Employee-Id") Long employeeId,
            @Valid @RequestBody LeaveRequestCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leaveService.createRequest(employeeId, request));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<LeaveRequestResponse>> getRequests(
            @RequestHeader("X-Employee-Id") Long employeeId) {
        return ResponseEntity.ok(leaveService.getRequests(employeeId));
    }
}
