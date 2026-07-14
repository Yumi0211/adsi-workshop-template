package com.example.attendance.alert.dto;

import java.util.List;

public record AlertPageResponse(
        List<AlertResponse> content,
        int page,
        int size,
        long totalElements
) {
}
