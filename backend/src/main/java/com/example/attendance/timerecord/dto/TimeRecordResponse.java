package com.example.attendance.timerecord.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.example.attendance.common.enums.RecordSource;
import com.example.attendance.common.enums.TimeRecordType;

public record TimeRecordResponse(
        Long id,
        Long employeeId,
        LocalDate recordDate,
        TimeRecordType type,
        OffsetDateTime recordedAt,
        RecordSource source
) {
}
