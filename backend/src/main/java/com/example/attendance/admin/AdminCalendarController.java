package com.example.attendance.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.attendance.admin.dto.WorkCalendarCreateRequest;
import com.example.attendance.admin.dto.WorkCalendarResponse;
import com.example.attendance.admin.dto.WorkCalendarUpdateRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/admin/calendars")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminCalendarController {

    private final WorkCalendarService workCalendarService;

    public AdminCalendarController(WorkCalendarService workCalendarService) {
        this.workCalendarService = workCalendarService;
    }

    @GetMapping
    public ResponseEntity<List<WorkCalendarResponse>> getCalendars(
            @RequestParam @Min(1900) @Max(2100) int fiscalYear) {
        return ResponseEntity.ok(workCalendarService.findByFiscalYear(fiscalYear));
    }

    @PostMapping
    public ResponseEntity<WorkCalendarResponse> create(@Valid @RequestBody WorkCalendarCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workCalendarService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkCalendarResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody WorkCalendarUpdateRequest request) {
        return ResponseEntity.ok(workCalendarService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workCalendarService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
