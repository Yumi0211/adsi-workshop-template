package com.example.attendance.admin.dto;

public record OvertimeRecordResponse(
        Long employeeId,
        String employeeName,
        String departmentName,
        int overtimeMinutes,
        int yearlyOvertimeMinutes,
        boolean isOverMonthlyLimit,
        boolean isOverYearlyLimit
) {}
