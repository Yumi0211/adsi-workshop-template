package com.example.attendance.leave.dto;

import java.time.LocalDate;

import com.example.attendance.leave.dto.LeaveRequestCreateRequest.LeaveType;

public record LeaveDetail(
        LocalDate leaveDate,
        LeaveType leaveType
) {}
