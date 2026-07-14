package com.example.attendance.leave;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.common.enums.RequestStatus;
import com.example.attendance.common.enums.RequestType;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.employee.EmployeeRepository;
import com.example.attendance.leave.dto.LeaveBalanceDetailResponse;
import com.example.attendance.leave.dto.LeaveBalanceResponse;
import com.example.attendance.leave.dto.LeaveRequestCreateRequest;
import com.example.attendance.leave.dto.LeaveRequestCreateRequest.LeaveType;
import com.example.attendance.leave.dto.LeaveRequestResponse;

@Service
@Transactional
public class LeaveServiceImpl implements LeaveService {

    private static final BigDecimal OBLIGATION_DAYS = new BigDecimal("5.0");

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final Supplier<LocalDate> todaySupplier;

    public LeaveServiceImpl(LeaveBalanceRepository leaveBalanceRepository,
                            ApprovalRequestRepository approvalRequestRepository,
                            EmployeeRepository employeeRepository,
                            Supplier<LocalDate> todaySupplier) {
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.approvalRequestRepository = approvalRequestRepository;
        this.employeeRepository = employeeRepository;
        this.todaySupplier = todaySupplier;
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveBalanceResponse getBalance(Long employeeId) {
        var today = todaySupplier.get();
        var balances = leaveBalanceRepository.findActiveByEmployeeId(employeeId, today);

        var totalGranted = balances.stream()
                .map(LeaveBalance::getGrantedDays)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var totalUsed = balances.stream()
                .map(LeaveBalance::getUsedDays)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var totalCarriedOver = balances.stream()
                .map(LeaveBalance::getCarriedOverDays)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var totalRemaining = balances.stream()
                .map(LeaveBalance::getRemainingDays)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var obligationRemaining = OBLIGATION_DAYS.subtract(totalUsed).max(BigDecimal.ZERO);
        int fiscalYear = computeFiscalYear(today);

        var details = balances.stream()
                .map(LeaveBalanceDetailResponse::from)
                .toList();

        return new LeaveBalanceResponse(
                fiscalYear, totalGranted, totalUsed, totalRemaining,
                totalCarriedOver, obligationRemaining, details
        );
    }

    @Override
    public LeaveRequestResponse createRequest(Long employeeId, LeaveRequestCreateRequest request) {
        var today = todaySupplier.get();
        var balances = leaveBalanceRepository.findActiveByEmployeeId(employeeId, today);

        var consumeDays = request.leaveType() == LeaveType.FULL
                ? new BigDecimal("1.0")
                : new BigDecimal("0.5");

        var totalRemaining = balances.stream()
                .map(LeaveBalance::getRemainingDays)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalRemaining.compareTo(consumeDays) < 0) {
            throw new BusinessException("有給残日数が不足しています");
        }

        consumeFromBalances(balances, consumeDays);

        var approverId = employeeRepository.findApproverIdByEmployeeId(employeeId)
                .orElse(employeeId);

        var detail = """
                {"leaveDate":"%s","leaveType":"%s"}""".formatted(request.leaveDate(), request.leaveType().name());

        var approvalRequest = ApprovalRequest.builder()
                .applicantId(employeeId)
                .approverId(approverId)
                .type(RequestType.LEAVE)
                .status(RequestStatus.PENDING)
                .requestDate(today)
                .detail(detail)
                .reason(request.reason())
                .build();

        approvalRequest = approvalRequestRepository.save(approvalRequest);

        return new LeaveRequestResponse(
                approvalRequest.getId(),
                approvalRequest.getStatus().name(),
                request.leaveDate(),
                request.leaveType().name()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getRequests(Long employeeId) {
        var requests = approvalRequestRepository.findByApplicantIdAndType(employeeId, RequestType.LEAVE);
        return requests.stream()
                .map(this::toLeaveRequestResponse)
                .toList();
    }

    private void consumeFromBalances(List<LeaveBalance> balances, BigDecimal days) {
        var sorted = balances.stream()
                .filter(b -> b.getRemainingDays().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(LeaveBalance::getExpiryDate))
                .toList();

        var remaining = days;
        for (var balance : sorted) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            var available = balance.getRemainingDays();
            var consume = available.min(remaining);
            balance.setUsedDays(balance.getUsedDays().add(consume));
            leaveBalanceRepository.save(balance);
            remaining = remaining.subtract(consume);
        }
    }

    private LeaveRequestResponse toLeaveRequestResponse(ApprovalRequest request) {
        var detail = request.getDetail();
        var leaveDate = extractJsonField(detail, "leaveDate");
        var leaveType = extractJsonField(detail, "leaveType");
        return new LeaveRequestResponse(
                request.getId(),
                request.getStatus().name(),
                LocalDate.parse(leaveDate),
                leaveType
        );
    }

    private String extractJsonField(String json, String field) {
        var key = "\"" + field + "\":\"";
        int start = json.indexOf(key) + key.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    static BigDecimal calculateGrantDays(int yearsOfService) {
        if (yearsOfService <= 0) return new BigDecimal("10");
        if (yearsOfService == 1) return new BigDecimal("11");
        if (yearsOfService == 2) return new BigDecimal("12");
        if (yearsOfService == 3) return new BigDecimal("14");
        if (yearsOfService == 4) return new BigDecimal("16");
        if (yearsOfService == 5) return new BigDecimal("18");
        return new BigDecimal("20");
    }

    private int computeFiscalYear(LocalDate date) {
        return date.getMonthValue() >= 4 ? date.getYear() : date.getYear() - 1;
    }
}
