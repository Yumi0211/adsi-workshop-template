package com.example.attendance.approval.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.example.attendance.approval.ApprovalRequest;

public record ApprovalRequestResponse(
        Long id,
        Long applicantId,
        Long approverId,
        String type,
        String status,
        LocalDate requestDate,
        String reason,
        OffsetDateTime approvedAt,
        String rejectionReason
) {
    public static ApprovalRequestResponse from(ApprovalRequest entity) {
        return new ApprovalRequestResponse(
                entity.getId(),
                entity.getApplicantId(),
                entity.getApproverId(),
                entity.getType().name(),
                entity.getStatus().name(),
                entity.getRequestDate(),
                entity.getReason(),
                entity.getApprovedAt(),
                entity.getRejectionReason()
        );
    }
}
