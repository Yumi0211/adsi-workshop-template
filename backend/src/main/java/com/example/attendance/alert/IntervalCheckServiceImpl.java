package com.example.attendance.alert;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.attendance.DailyAttendanceRepository;
import com.example.attendance.common.enums.AlertType;

@Service
@Transactional
public class IntervalCheckServiceImpl implements IntervalCheckService {

    private static final int MINIMUM_INTERVAL_HOURS = 11;
    private static final ZoneOffset JST = ZoneOffset.ofHours(9);

    private final DailyAttendanceRepository dailyAttendanceRepository;
    private final AlertRepository alertRepository;

    public IntervalCheckServiceImpl(DailyAttendanceRepository dailyAttendanceRepository,
                                    AlertRepository alertRepository) {
        this.dailyAttendanceRepository = dailyAttendanceRepository;
        this.alertRepository = alertRepository;
    }

    @Override
    public void checkInterval(Long employeeId, LocalDate date) {
        var yesterday = date.minusDays(1);

        var attendances = dailyAttendanceRepository.findByEmployeeIdAndMonth(
                employeeId, yesterday, date);

        var yesterdayRecord = attendances.stream()
                .filter(a -> a.getAttendanceDate().equals(yesterday))
                .findFirst().orElse(null);
        var todayRecord = attendances.stream()
                .filter(a -> a.getAttendanceDate().equals(date))
                .findFirst().orElse(null);

        if (yesterdayRecord == null || todayRecord == null) {
            return;
        }
        if (yesterdayRecord.getClockOut() == null || todayRecord.getClockIn() == null) {
            return;
        }

        long intervalHours = Duration.between(
                yesterdayRecord.getClockOut(), todayRecord.getClockIn()).toHours();

        if (intervalHours >= MINIMUM_INTERVAL_HOURS) {
            return;
        }

        var dayStart = date.atStartOfDay().atOffset(JST);
        if (alertRepository.existsByEmployeeIdAndTypeAndCreatedAtAfter(
                employeeId, AlertType.INTERVAL_VIOLATION, dayStart)) {
            return;
        }

        var alert = Alert.builder()
                .employeeId(employeeId)
                .type(AlertType.INTERVAL_VIOLATION)
                .message(String.format("勤務間インターバルが11時間未満です（%d時間）", intervalHours))
                .build();
        alertRepository.save(alert);
    }
}
