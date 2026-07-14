package com.example.attendance.alert;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.attendance.DailyAttendanceRepository;
import com.example.attendance.common.enums.AlertType;

@Service
@Transactional
public class OvertimeAlertServiceImpl implements OvertimeAlertService {

    private static final int MONTHLY_LIMIT_MINUTES = 45 * 60;
    private static final int YEARLY_LIMIT_MINUTES = 360 * 60;
    private static final ZoneOffset JST = ZoneOffset.ofHours(9);

    private final DailyAttendanceRepository dailyAttendanceRepository;
    private final AlertRepository alertRepository;

    public OvertimeAlertServiceImpl(DailyAttendanceRepository dailyAttendanceRepository,
                                    AlertRepository alertRepository) {
        this.dailyAttendanceRepository = dailyAttendanceRepository;
        this.alertRepository = alertRepository;
    }

    @Override
    public void checkMonthlyOvertime(Long employeeId, int year, int month) {
        var startDate = LocalDate.of(year, month, 1);
        var endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        var attendances = dailyAttendanceRepository.findByEmployeeIdAndMonth(
                employeeId, startDate, endDate);

        int totalOvertimeMinutes = attendances.stream()
                .mapToInt(a -> a.getOvertimeMinutes())
                .sum();

        if (totalOvertimeMinutes <= MONTHLY_LIMIT_MINUTES) {
            return;
        }

        var monthStart = startDate.atStartOfDay().atOffset(JST);
        if (alertRepository.existsByEmployeeIdAndTypeAndCreatedAtAfter(
                employeeId, AlertType.OVERTIME_MONTHLY, monthStart)) {
            return;
        }

        var alert = Alert.builder()
                .employeeId(employeeId)
                .type(AlertType.OVERTIME_MONTHLY)
                .message(String.format("月間残業時間が45時間を超過しました（%d時間%d分）",
                        totalOvertimeMinutes / 60, totalOvertimeMinutes % 60))
                .build();
        alertRepository.save(alert);
    }

    @Override
    public void checkYearlyOvertime(Long employeeId, int fiscalYear) {
        var startDate = LocalDate.of(fiscalYear, 4, 1);
        var endDate = LocalDate.of(fiscalYear + 1, 3, 31);

        var attendances = dailyAttendanceRepository.findByEmployeeIdAndMonth(
                employeeId, startDate, endDate);

        int totalOvertimeMinutes = attendances.stream()
                .mapToInt(a -> a.getOvertimeMinutes())
                .sum();

        if (totalOvertimeMinutes <= YEARLY_LIMIT_MINUTES) {
            return;
        }

        var yearStart = startDate.atStartOfDay().atOffset(JST);
        if (alertRepository.existsByEmployeeIdAndTypeAndCreatedAtAfter(
                employeeId, AlertType.OVERTIME_YEARLY, yearStart)) {
            return;
        }

        var alert = Alert.builder()
                .employeeId(employeeId)
                .type(AlertType.OVERTIME_YEARLY)
                .message(String.format("年間残業時間が360時間を超過しました（%d時間%d分）",
                        totalOvertimeMinutes / 60, totalOvertimeMinutes % 60))
                .build();
        alertRepository.save(alert);
    }
}
