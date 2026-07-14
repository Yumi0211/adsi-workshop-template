package com.example.attendance.approval.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectRequest(
        @NotBlank String rejectionReason
) {
}
