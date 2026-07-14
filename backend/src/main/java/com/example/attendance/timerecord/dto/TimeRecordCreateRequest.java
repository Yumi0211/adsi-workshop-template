package com.example.attendance.timerecord.dto;

import com.example.attendance.common.enums.RecordSource;
import com.example.attendance.common.enums.TimeRecordType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TimeRecordCreateRequest(
        @NotNull(message = "打刻種別は必須です")
        TimeRecordType type,

        @NotNull(message = "打刻元は必須です")
        RecordSource source,

        @Size(max = 50, message = "カードIDは50文字以内で入力してください")
        String cardId
) {
}
