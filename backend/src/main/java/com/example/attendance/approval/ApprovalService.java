package com.example.attendance.approval;

import java.util.List;

import com.example.attendance.approval.dto.ApprovalRequestDetailResponse;
import com.example.attendance.approval.dto.ApprovalRequestResponse;
import com.example.attendance.approval.dto.TimeCorrectionCreateRequest;

public interface ApprovalService {

    ApprovalRequestResponse createTimeCorrection(Long applicantId, TimeCorrectionCreateRequest request);

    List<ApprovalRequestResponse> getPendingRequests(Long approverId);

    ApprovalRequestDetailResponse getRequestDetail(Long requestId);

    ApprovalRequestResponse approve(Long approverId, Long requestId);

    ApprovalRequestResponse reject(Long approverId, Long requestId, String rejectionReason);

    List<ApprovalRequestResponse> getMyRequests(Long applicantId);
}
