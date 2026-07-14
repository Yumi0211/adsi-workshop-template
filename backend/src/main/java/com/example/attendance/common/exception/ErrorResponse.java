package com.example.attendance.common.exception;

import java.util.List;

public record ErrorResponse(
        String type,
        String title,
        int status,
        String detail,
        List<FieldError> errors
) {
    public record FieldError(String field, String message) {}
}
