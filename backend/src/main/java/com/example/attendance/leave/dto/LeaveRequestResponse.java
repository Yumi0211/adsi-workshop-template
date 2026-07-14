package com.example.attendance.leave.dto;

import java.time.LocalDate;

public record LeaveRequestResponse(
        Long id,
        String status,
        LocalDate leaveDate,
        String leaveType
) {}
