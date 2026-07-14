package com.example.attendance.leave;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.example.attendance.common.enums.RequestStatus;
import com.example.attendance.common.enums.RequestType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "approval_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    @Column(name = "approver_id", nullable = false)
    private Long approverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(nullable = false, columnDefinition = "jsonb")
    private String detail;

    @Column
    private String reason;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
