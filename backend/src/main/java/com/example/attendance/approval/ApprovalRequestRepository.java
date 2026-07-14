package com.example.attendance.approval;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.attendance.common.enums.RequestStatus;
import com.example.attendance.common.enums.RequestType;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {

    List<ApprovalRequest> findByApplicantIdAndType(Long applicantId, RequestType type);

    List<ApprovalRequest> findByApplicantId(Long applicantId);

    List<ApprovalRequest> findByApproverIdAndStatus(Long approverId, RequestStatus status);
}
