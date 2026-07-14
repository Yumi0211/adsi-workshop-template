package com.example.attendance.timerecord.dto;

import com.example.attendance.common.enums.RecordSource;
import com.example.attendance.common.enums.TimeRecordType;

import jakarta.validation.constraints.NotNull;

public record TimeRecordCreateRequest(
        @NotNull(message = "打刻種別は必須です")
        TimeRecordType type,

        @NotNull(message = "打刻元は必須です")
        RecordSource source,

        String cardId
) {
}
