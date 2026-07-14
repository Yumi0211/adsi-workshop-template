package com.example.attendance.alert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.common.enums.AlertType;
import com.example.attendance.leave.LeaveBalance;
import com.example.attendance.leave.LeaveBalanceRepository;

@Service
@Transactional
public class LeaveObligationAlertServiceImpl implements LeaveObligationAlertService {

    private static final BigDecimal OBLIGATION_DAYS = new BigDecimal("5.0");
    private static final BigDecimal ALERT_THRESHOLD = new BigDecimal("3.0");
    private static final ZoneOffset JST = ZoneOffset.ofHours(9);

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final AlertRepository alertRepository;

    public LeaveObligationAlertServiceImpl(LeaveBalanceRepository leaveBalanceRepository,
                                           AlertRepository alertRepository) {
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.alertRepository = alertRepository;
    }

    @Override
    public void checkLeaveObligation(Long employeeId, int fiscalYear) {
        List<LeaveBalance> balances = leaveBalanceRepository.findByEmployeeIdAndFiscalYear(
                employeeId, fiscalYear);

        if (balances.isEmpty()) {
            return;
        }

        BigDecimal totalUsedDays = balances.stream()
                .map(LeaveBalance::getUsedDays)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalUsedDays.compareTo(ALERT_THRESHOLD) >= 0) {
            return;
        }

        var fiscalYearStart = LocalDate.of(fiscalYear, 4, 1)
                .atStartOfDay().atOffset(JST);
        if (alertRepository.existsByEmployeeIdAndTypeAndCreatedAtAfter(
                employeeId, AlertType.LEAVE_OBLIGATION, fiscalYearStart)) {
            return;
        }

        var alert = Alert.builder()
                .employeeId(employeeId)
                .type(AlertType.LEAVE_OBLIGATION)
                .message(String.format(
                        "年次有給休暇の取得が5日未満です（現在%.1f日取得）。年度内に5日以上の取得が必要です。",
                        totalUsedDays))
                .build();
        alertRepository.save(alert);
    }
}
