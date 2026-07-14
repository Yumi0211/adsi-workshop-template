package com.example.attendance.approval;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.example.attendance.approval.dto.ApprovalRequestDetailResponse;
import com.example.attendance.approval.dto.ApprovalRequestResponse;
import com.example.attendance.approval.dto.RejectRequest;
import com.example.attendance.approval.dto.TimeCorrectionCreateRequest;

@RestController
@RequestMapping("/api/v1")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping("/time-corrections")
    @ResponseStatus(HttpStatus.CREATED)
    public ApprovalRequestResponse createTimeCorrection(
            @RequestParam Long applicantId,
            @Valid @RequestBody TimeCorrectionCreateRequest request) {
        return approvalService.createTimeCorrection(applicantId, request);
    }

    @GetMapping("/approvals/pending")
    public List<ApprovalRequestResponse> getPendingRequests(@RequestParam Long approverId) {
        return approvalService.getPendingRequests(approverId);
    }

    @GetMapping("/approvals/{id}")
    public ApprovalRequestDetailResponse getRequestDetail(
            @RequestParam Long requesterId,
            @PathVariable Long id) {
        return approvalService.getRequestDetail(requesterId, id);
    }

    @PutMapping("/approvals/{id}/approve")
    public ApprovalRequestResponse approve(
            @RequestParam Long approverId,
            @PathVariable Long id) {
        return approvalService.approve(approverId, id);
    }

    @PutMapping("/approvals/{id}/reject")
    public ApprovalRequestResponse reject(
            @RequestParam Long approverId,
            @PathVariable Long id,
            @Valid @RequestBody RejectRequest request) {
        return approvalService.reject(approverId, id, request.rejectionReason());
    }

    @GetMapping("/my-requests")
    public List<ApprovalRequestResponse> getMyRequests(@RequestParam Long applicantId) {
        return approvalService.getMyRequests(applicantId);
    }
}
