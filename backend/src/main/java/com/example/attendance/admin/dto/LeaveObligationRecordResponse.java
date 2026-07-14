package com.example.attendance.admin.dto;

import java.math.BigDecimal;

public record LeaveObligationRecordResponse(
        Long employeeId,
        String employeeName,
        String departmentName,
        BigDecimal usedDays,
        BigDecimal obligationRemaining
) {}
