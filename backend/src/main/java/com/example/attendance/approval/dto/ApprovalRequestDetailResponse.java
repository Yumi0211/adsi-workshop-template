package com.example.attendance.approval.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.example.attendance.approval.ApprovalRequest;

public record ApprovalRequestDetailResponse(
        Long id,
        Long applicantId,
        Long approverId,
        String type,
        String status,
        LocalDate requestDate,
        String detail,
        String reason,
        OffsetDateTime approvedAt,
        String rejectionReason
) {
    public static ApprovalRequestDetailResponse from(ApprovalRequest entity) {
        return new ApprovalRequestDetailResponse(
                entity.getId(),
                entity.getApplicantId(),
                entity.getApproverId(),
                entity.getType().name(),
                entity.getStatus().name(),
                entity.getRequestDate(),
                entity.getDetail(),
                entity.getReason(),
                entity.getApprovedAt(),
                entity.getRejectionReason()
        );
    }
}
