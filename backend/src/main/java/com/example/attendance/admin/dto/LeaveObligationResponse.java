package com.example.attendance.admin.dto;

import java.util.List;

public record LeaveObligationResponse(
        int fiscalYear,
        List<LeaveObligationRecordResponse> records
) {}
