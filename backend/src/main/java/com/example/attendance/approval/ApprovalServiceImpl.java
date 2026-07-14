package com.example.attendance.approval;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.attendance.approval.dto.ApprovalRequestDetailResponse;
import com.example.attendance.approval.dto.ApprovalRequestResponse;
import com.example.attendance.approval.dto.TimeCorrectionCreateRequest;
import com.example.attendance.attendance.AttendanceCalculationService;
import com.example.attendance.attendance.DailyAttendance;
import com.example.attendance.attendance.DailyAttendanceRepository;
import com.example.attendance.common.enums.RequestStatus;
import com.example.attendance.common.enums.RequestType;
import com.example.attendance.common.enums.TimeRecordType;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.employee.EmployeeRepository;
import com.example.attendance.timerecord.TimeRecord;
import com.example.attendance.timerecord.TimeRecordRepository;

@Service
@Transactional
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final TimeRecordRepository timeRecordRepository;
    private final DailyAttendanceRepository dailyAttendanceRepository;
    private final AttendanceCalculationService attendanceCalculationService;
    private final Supplier<LocalDate> todaySupplier;
    private final Supplier<OffsetDateTime> nowSupplier;
    private final ObjectMapper objectMapper;

    public ApprovalServiceImpl(ApprovalRequestRepository approvalRequestRepository,
                               EmployeeRepository employeeRepository,
                               TimeRecordRepository timeRecordRepository,
                               DailyAttendanceRepository dailyAttendanceRepository,
                               AttendanceCalculationService attendanceCalculationService,
                               Supplier<LocalDate> todaySupplier,
                               Supplier<OffsetDateTime> nowSupplier,
                               ObjectMapper objectMapper) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.employeeRepository = employeeRepository;
        this.timeRecordRepository = timeRecordRepository;
        this.dailyAttendanceRepository = dailyAttendanceRepository;
        this.attendanceCalculationService = attendanceCalculationService;
        this.todaySupplier = todaySupplier;
        this.nowSupplier = nowSupplier;
        this.objectMapper = objectMapper;
    }

    @Override
    public ApprovalRequestResponse createTimeCorrection(Long applicantId, TimeCorrectionCreateRequest request) {
        if (request.correctedClockIn() == null && request.correctedClockOut() == null) {
            throw new BusinessException("修正後の出勤時刻または退勤時刻のいずれかは必須です");
        }

        var approverId = employeeRepository.findApproverIdByEmployeeId(applicantId)
                .orElseThrow(() -> new BusinessException("承認者が見つかりません"));

        var detail = serializeDetail(request);

        var saved = approvalRequestRepository.save(ApprovalRequest.builder()
                .applicantId(applicantId)
                .approverId(approverId)
                .type(RequestType.TIME_CORRECTION)
                .status(RequestStatus.PENDING)
                .requestDate(todaySupplier.get())
                .detail(detail)
                .reason(request.reason())
                .build());

        return ApprovalRequestResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalRequestResponse> getPendingRequests(Long approverId) {
        return approvalRequestRepository
                .findByApproverIdAndStatus(approverId, RequestStatus.PENDING)
                .stream()
                .map(ApprovalRequestResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalRequestDetailResponse getRequestDetail(Long requesterId, Long requestId) {
        var request = approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalRequest", requestId));

        if (!request.getApplicantId().equals(requesterId) && !request.getApproverId().equals(requesterId)) {
            throw new BusinessException("この申請を閲覧する権限がありません");
        }

        return ApprovalRequestDetailResponse.from(request);
    }

    @Override
    public ApprovalRequestResponse approve(Long approverId, Long requestId) {
        var request = approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalRequest", requestId));

        validateApproval(approverId, request);

        request.setStatus(RequestStatus.APPROVED);
        request.setApprovedAt(nowSupplier.get());
        request.setUpdatedAt(nowSupplier.get());
        var saved = approvalRequestRepository.save(request);

        applySideEffect(saved);

        return ApprovalRequestResponse.from(saved);
    }

    @Override
    public ApprovalRequestResponse reject(Long approverId, Long requestId, String rejectionReason) {
        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new BusinessException("却下理由は必須です");
        }

        var request = approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalRequest", requestId));

        validateRejection(approverId, request);

        request.setStatus(RequestStatus.REJECTED);
        request.setRejectionReason(rejectionReason);
        request.setUpdatedAt(nowSupplier.get());
        var saved = approvalRequestRepository.save(request);

        return ApprovalRequestResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalRequestResponse> getMyRequests(Long applicantId) {
        return approvalRequestRepository
                .findByApplicantId(applicantId)
                .stream()
                .map(ApprovalRequestResponse::from)
                .toList();
    }

    private void validateApproval(Long approverId, ApprovalRequest request) {
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BusinessException("PENDING 状態の申請のみ承認できます");
        }
        if (!request.getApproverId().equals(approverId)) {
            throw new BusinessException("この申請の承認者ではありません");
        }
        if (request.getApplicantId().equals(approverId)) {
            throw new BusinessException("自分自身の申請は承認できません");
        }
    }

    private void validateRejection(Long approverId, ApprovalRequest request) {
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BusinessException("PENDING 状態の申請のみ却下できます");
        }
        if (!request.getApproverId().equals(approverId)) {
            throw new BusinessException("この申請の承認者ではありません");
        }
    }

    private void applySideEffect(ApprovalRequest request) {
        switch (request.getType()) {
            case TIME_CORRECTION -> applyTimeCorrectionSideEffect(request);
            case LEAVE -> { /* 有給は申請時に残高消化済み */ }
            case OVERTIME -> { /* アラート解除は Unit 06 で実装 */ }
        }
    }

    private void applyTimeCorrectionSideEffect(ApprovalRequest request) {
        var correction = deserializeTimeCorrectionDetail(request.getDetail());
        var targetDate = correction.targetDate();
        var employeeId = request.getApplicantId();

        var records = timeRecordRepository
                .findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(employeeId, targetDate);

        updateTimeRecord(records, TimeRecordType.CLOCK_IN, correction.correctedClockIn());
        updateTimeRecord(records, TimeRecordType.CLOCK_OUT, correction.correctedClockOut());

        DailyAttendance recalculated = attendanceCalculationService.calculate(employeeId, targetDate);
        dailyAttendanceRepository.save(recalculated);
    }

    private void updateTimeRecord(List<TimeRecord> records, TimeRecordType type, OffsetDateTime correctedTime) {
        if (correctedTime == null) {
            return;
        }
        records.stream()
                .filter(r -> r.getType() == type)
                .findFirst()
                .ifPresent(r -> {
                    r.setRecordedAt(correctedTime);
                    timeRecordRepository.save(r);
                });
    }

    private String serializeDetail(Object detail) {
        try {
            return objectMapper.writeValueAsString(detail);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize detail", e);
        }
    }

    private TimeCorrectionCreateRequest deserializeTimeCorrectionDetail(String json) {
        try {
            return objectMapper.readValue(json, TimeCorrectionCreateRequest.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize time correction detail", e);
        }
    }
}
