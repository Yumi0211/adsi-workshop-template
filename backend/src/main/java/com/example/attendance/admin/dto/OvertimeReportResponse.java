package com.example.attendance.admin.dto;

import java.util.List;

public record OvertimeReportResponse(
        int year,
        int month,
        List<OvertimeRecordResponse> records
) {}
