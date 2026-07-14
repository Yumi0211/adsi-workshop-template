package com.example.attendance.approval;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.example.attendance.approval.dto.TimeCorrectionCreateRequest;
import com.example.attendance.attendance.AttendanceCalculationService;
import com.example.attendance.attendance.DailyAttendance;
import com.example.attendance.attendance.DailyAttendanceRepository;
import com.example.attendance.common.enums.AttendanceStatus;
import com.example.attendance.common.enums.RequestStatus;
import com.example.attendance.common.enums.RequestType;
import com.example.attendance.common.enums.TimeRecordType;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.employee.EmployeeRepository;
import com.example.attendance.timerecord.TimeRecord;
import com.example.attendance.timerecord.TimeRecordRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private ApprovalRequestRepository approvalRequestRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private TimeRecordRepository timeRecordRepository;
    @Mock
    private DailyAttendanceRepository dailyAttendanceRepository;
    @Mock
    private AttendanceCalculationService attendanceCalculationService;

    private static final OffsetDateTime FIXED_NOW = OffsetDateTime.of(2026, 7, 14, 10, 0, 0, 0, ZoneOffset.ofHours(9));
    private final Supplier<LocalDate> todaySupplier = () -> LocalDate.of(2026, 7, 14);
    private final Supplier<OffsetDateTime> nowSupplier = () -> FIXED_NOW;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private ApprovalServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ApprovalServiceImpl(
                approvalRequestRepository,
                employeeRepository,
                timeRecordRepository,
                dailyAttendanceRepository,
                attendanceCalculationService,
                todaySupplier,
                nowSupplier,
                objectMapper
        );
    }

    @Test
    @DisplayName("打刻修正申請: 有効なリクエストで PENDING 状態の申請が作成される")
    void createTimeCorrection_validRequest_createsPendingRequest() {
        var request = new TimeCorrectionCreateRequest(
                LocalDate.of(2026, 7, 10),
                OffsetDateTime.parse("2026-07-10T08:30:00+09:00"),
                null,
                "電車遅延のため"
        );

        when(employeeRepository.findApproverIdByEmployeeId(1L)).thenReturn(Optional.of(2L));
        when(approvalRequestRepository.save(any(ApprovalRequest.class)))
                .thenAnswer(invocation -> {
                    var saved = invocation.<ApprovalRequest>getArgument(0);
                    saved.setId(100L);
                    return saved;
                });

        var result = service.createTimeCorrection(1L, request);

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.type()).isEqualTo("TIME_CORRECTION");
        assertThat(result.applicantId()).isEqualTo(1L);
        assertThat(result.approverId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("打刻修正申請: 修正時刻が両方 null の場合はエラー")
    void createTimeCorrection_bothNull_throwsException() {
        var request = new TimeCorrectionCreateRequest(
                LocalDate.of(2026, 7, 10), null, null, "理由"
        );

        assertThatThrownBy(() -> service.createTimeCorrection(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("修正後の出勤時刻または退勤時刻のいずれかは必須です");
    }

    @Test
    @DisplayName("打刻修正申請: 承認者が見つからない場合は例外")
    void createTimeCorrection_noApprover_throwsException() {
        var request = new TimeCorrectionCreateRequest(
                LocalDate.of(2026, 7, 10),
                OffsetDateTime.parse("2026-07-10T09:00:00+09:00"),
                null, "理由"
        );

        when(employeeRepository.findApproverIdByEmployeeId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createTimeCorrection(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("承認者が見つかりません");
    }

    @Test
    @DisplayName("申請詳細: 申請者は自分の申請を閲覧できる")
    void getRequestDetail_byApplicant_returnsDetail() {
        var request = buildPendingRequest(10L, 20L, RequestType.TIME_CORRECTION);
        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        var result = service.getRequestDetail(10L, 1L);

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("申請詳細: 承認者は担当申請を閲覧できる")
    void getRequestDetail_byApprover_returnsDetail() {
        var request = buildPendingRequest(10L, 20L, RequestType.TIME_CORRECTION);
        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        var result = service.getRequestDetail(20L, 1L);

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("申請詳細: 無関係なユーザーは閲覧不可")
    void getRequestDetail_byUnrelatedUser_throwsException() {
        var request = buildPendingRequest(10L, 20L, RequestType.TIME_CORRECTION);
        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.getRequestDetail(99L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("この申請を閲覧する権限がありません");
    }

    @Test
    @DisplayName("承認: PENDING の申請を承認すると APPROVED になる")
    void approve_pendingRequest_changesStatusToApproved() {
        var request = buildPendingRequest(1L, 2L, RequestType.LEAVE);

        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(approvalRequestRepository.save(any(ApprovalRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.approve(2L, 1L);

        assertThat(result.status()).isEqualTo("APPROVED");
        assertThat(result.approvedAt()).isEqualTo(FIXED_NOW);
    }

    @Test
    @DisplayName("承認: PENDING 以外のステータスは承認不可")
    void approve_nonPendingRequest_throwsException() {
        var request = buildPendingRequest(1L, 2L, RequestType.LEAVE);
        request.setStatus(RequestStatus.APPROVED);

        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.approve(2L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("PENDING 状態の申請のみ承認できます");
    }

    @Test
    @DisplayName("承認: 承認者以外は承認不可")
    void approve_byNonApprover_throwsException() {
        var request = buildPendingRequest(1L, 2L, RequestType.LEAVE);

        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.approve(3L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("この申請の承認者ではありません");
    }

    @Test
    @DisplayName("承認: 自分自身の申請は承認不可")
    void approve_selfApproval_throwsException() {
        var request = buildPendingRequest(2L, 2L, RequestType.LEAVE);

        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.approve(2L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("自分自身の申請は承認できません");
    }

    @Test
    @DisplayName("承認: 打刻修正承認で TimeRecord が修正され DailyAttendance が再計算される")
    void approve_timeCorrection_recalculatesAttendance() throws Exception {
        var targetDate = LocalDate.of(2026, 7, 10);
        var correctedClockIn = OffsetDateTime.parse("2026-07-10T08:30:00+09:00");
        var detail = objectMapper.writeValueAsString(
                new TimeCorrectionCreateRequest(targetDate, correctedClockIn, null, "修正")
        );

        var request = ApprovalRequest.builder()
                .id(1L)
                .applicantId(10L)
                .approverId(20L)
                .type(RequestType.TIME_CORRECTION)
                .status(RequestStatus.PENDING)
                .requestDate(LocalDate.of(2026, 7, 14))
                .detail(detail)
                .build();

        var clockInRecord = TimeRecord.builder()
                .id(100L)
                .employeeId(10L)
                .recordDate(targetDate)
                .type(TimeRecordType.CLOCK_IN)
                .recordedAt(OffsetDateTime.parse("2026-07-10T09:00:00+09:00"))
                .build();

        var recalculated = DailyAttendance.builder()
                .employeeId(10L)
                .attendanceDate(targetDate)
                .status(AttendanceStatus.PRESENT)
                .workingMinutes(480)
                .build();

        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(approvalRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(10L, targetDate))
                .thenReturn(List.of(clockInRecord));
        when(attendanceCalculationService.calculate(10L, targetDate)).thenReturn(recalculated);

        service.approve(20L, 1L);

        assertThat(clockInRecord.getRecordedAt()).isEqualTo(correctedClockIn);
        verify(timeRecordRepository).save(clockInRecord);
        verify(dailyAttendanceRepository).save(recalculated);
    }

    @Test
    @DisplayName("承認: 有給申請承認では追加の残高操作は行わない")
    void approve_leave_noAdditionalBalanceChange() {
        var request = buildPendingRequest(1L, 2L, RequestType.LEAVE);

        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(approvalRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.approve(2L, 1L);

        verify(timeRecordRepository, never()).save(any());
        verify(dailyAttendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("却下: PENDING の申請を却下すると REJECTED になり理由が保存される")
    void reject_pendingRequest_changesStatusToRejected() {
        var request = buildPendingRequest(1L, 2L, RequestType.TIME_CORRECTION);

        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(approvalRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.reject(2L, 1L, "修正内容が不明確");

        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(result.rejectionReason()).isEqualTo("修正内容が不明確");
    }

    @Test
    @DisplayName("却下: 却下理由なしはエラー")
    void reject_withoutReason_throwsException() {
        assertThatThrownBy(() -> service.reject(2L, 1L, ""))
                .isInstanceOf(BusinessException.class)
                .hasMessage("却下理由は必須です");
    }

    @Test
    @DisplayName("却下: 承認者以外は却下不可")
    void reject_byNonApprover_throwsException() {
        var request = buildPendingRequest(1L, 2L, RequestType.TIME_CORRECTION);
        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.reject(3L, 1L, "理由"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("この申請の承認者ではありません");
    }

    @Test
    @DisplayName("却下: PENDING 以外は却下不可")
    void reject_nonPendingRequest_throwsException() {
        var request = buildPendingRequest(1L, 2L, RequestType.TIME_CORRECTION);
        request.setStatus(RequestStatus.APPROVED);
        when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.reject(2L, 1L, "理由"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("PENDING 状態の申請のみ却下できます");
    }

    @Test
    @DisplayName("未処理一覧: 承認者の PENDING 申請のみ返す")
    void getPendingRequests_returnsOnlyPending() {
        var pending1 = buildPendingRequest(1L, 2L, RequestType.TIME_CORRECTION);
        pending1.setId(10L);
        var pending2 = buildPendingRequest(3L, 2L, RequestType.LEAVE);
        pending2.setId(11L);

        when(approvalRequestRepository.findByApproverIdAndStatus(2L, RequestStatus.PENDING))
                .thenReturn(List.of(pending1, pending2));

        var result = service.getPendingRequests(2L);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> r.status().equals("PENDING"));
    }

    @Test
    @DisplayName("自分の申請一覧: 申請者の全申請を返す")
    void getMyRequests_returnsApplicantRequests() {
        var req1 = buildPendingRequest(1L, 2L, RequestType.TIME_CORRECTION);
        req1.setId(10L);
        var req2 = buildPendingRequest(1L, 2L, RequestType.LEAVE);
        req2.setId(11L);
        req2.setStatus(RequestStatus.APPROVED);

        when(approvalRequestRepository.findByApplicantId(1L)).thenReturn(List.of(req1, req2));

        var result = service.getMyRequests(1L);

        assertThat(result).hasSize(2);
    }

    private ApprovalRequest buildPendingRequest(Long applicantId, Long approverId, RequestType type) {
        return ApprovalRequest.builder()
                .id(1L)
                .applicantId(applicantId)
                .approverId(approverId)
                .type(type)
                .status(RequestStatus.PENDING)
                .requestDate(LocalDate.of(2026, 7, 14))
                .detail("{}")
                .build();
    }
}
