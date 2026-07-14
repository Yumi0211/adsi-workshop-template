package com.example.attendance.timerecord;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.attendance.employee.EmployeeRepository;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.timerecord.dto.TimeRecordCreateRequest;
import com.example.attendance.timerecord.dto.TimeRecordResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/time-records")
public class TimeRecordController {

    private final TimeRecordService timeRecordService;
    private final EmployeeRepository employeeRepository;

    public TimeRecordController(TimeRecordService timeRecordService, EmployeeRepository employeeRepository) {
        this.timeRecordService = timeRecordService;
        this.employeeRepository = employeeRepository;
    }

    @PostMapping
    public ResponseEntity<TimeRecordResponse> create(
            @Valid @RequestBody TimeRecordCreateRequest request,
            Principal principal) {
        Long employeeId = resolveEmployeeId(principal);
        var response = timeRecordService.create(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TimeRecordResponse>> findByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Principal principal) {
        Long employeeId = resolveEmployeeId(principal);
        var records = timeRecordService.findByEmployeeIdAndDate(employeeId, date);
        return ResponseEntity.ok(records);
    }

    private Long resolveEmployeeId(Principal principal) {
        var employee = employeeRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", principal.getName()));
        return employee.getId();
    }
}
