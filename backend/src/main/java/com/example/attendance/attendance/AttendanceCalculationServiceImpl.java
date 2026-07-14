package com.example.attendance.attendance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.attendance.vo.TimeRange;
import com.example.attendance.common.enums.AttendanceStatus;
import com.example.attendance.common.enums.DayType;
import com.example.attendance.common.enums.TimeRecordType;
import com.example.attendance.timerecord.TimeRecord;
import com.example.attendance.timerecord.TimeRecordRepository;

@Service
@Transactional
public class AttendanceCalculationServiceImpl implements AttendanceCalculationService {

    private static final int STANDARD_WORK_MINUTES = 450;

    private final TimeRecordRepository timeRecordRepository;
    private final WorkCalendarRepository workCalendarRepository;
    private final DailyAttendanceRepository dailyAttendanceRepository;

    public AttendanceCalculationServiceImpl(
            TimeRecordRepository timeRecordRepository,
            WorkCalendarRepository workCalendarRepository,
            DailyAttendanceRepository dailyAttendanceRepository) {
        this.timeRecordRepository = timeRecordRepository;
        this.workCalendarRepository = workCalendarRepository;
        this.dailyAttendanceRepository = dailyAttendanceRepository;
    }

    @Override
    public DailyAttendance calculate(Long employeeId, LocalDate date) {
        List<TimeRecord> records = timeRecordRepository
                .findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(employeeId, date);
        boolean isHoliday = isHoliday(date);

        if (records.isEmpty()) {
            return buildEmptyAttendance(employeeId, date, isHoliday);
        }

        OffsetDateTime clockIn = findFirst(records, TimeRecordType.CLOCK_IN);
        OffsetDateTime clockOut = findFirst(records, TimeRecordType.CLOCK_OUT);

        if (clockIn == null || clockOut == null) {
            return buildPendingAttendance(employeeId, date, clockIn, isHoliday);
        }

        int breakMinutes = calculateBreakMinutes(records);
        int totalMinutes = (int) java.time.Duration.between(clockIn, clockOut).toMinutes();
        int workingMinutes = totalMinutes - breakMinutes;
        int overtimeMinutes = Math.max(workingMinutes - STANDARD_WORK_MINUTES, 0);
        int nightMinutes = calculateNightMinutes(clockIn, clockOut, date);

        return DailyAttendance.builder()
                .employeeId(employeeId)
                .attendanceDate(date)
                .clockIn(clockIn)
                .clockOut(clockOut)
                .breakMinutes(breakMinutes)
                .workingMinutes(workingMinutes)
                .overtimeMinutes(overtimeMinutes)
                .nightMinutes(nightMinutes)
                .holidayWork(isHoliday)
                .status(AttendanceStatus.PRESENT)
                .build();
    }

    private boolean isHoliday(LocalDate date) {
        return workCalendarRepository.findByCalendarDate(date)
                .map(wc -> wc.getDayType() == DayType.HOLIDAY || wc.getDayType() == DayType.COMPANY_HOLIDAY)
                .orElse(false);
    }

    private DailyAttendance buildEmptyAttendance(Long employeeId, LocalDate date, boolean isHoliday) {
        AttendanceStatus status = isHoliday ? AttendanceStatus.HOLIDAY : AttendanceStatus.ABSENT;
        return DailyAttendance.builder()
                .employeeId(employeeId)
                .attendanceDate(date)
                .status(status)
                .build();
    }

    private DailyAttendance buildPendingAttendance(Long employeeId, LocalDate date,
                                                    OffsetDateTime clockIn, boolean isHoliday) {
        return DailyAttendance.builder()
                .employeeId(employeeId)
                .attendanceDate(date)
                .clockIn(clockIn)
                .status(AttendanceStatus.PRESENT)
                .holidayWork(isHoliday)
                .build();
    }

    private OffsetDateTime findFirst(List<TimeRecord> records, TimeRecordType type) {
        return records.stream()
                .filter(r -> r.getType() == type)
                .map(TimeRecord::getRecordedAt)
                .findFirst()
                .orElse(null);
    }

    private int calculateBreakMinutes(List<TimeRecord> records) {
        List<OffsetDateTime> breakStarts = records.stream()
                .filter(r -> r.getType() == TimeRecordType.BREAK_START)
                .map(TimeRecord::getRecordedAt)
                .toList();
        List<OffsetDateTime> breakEnds = records.stream()
                .filter(r -> r.getType() == TimeRecordType.BREAK_END)
                .map(TimeRecord::getRecordedAt)
                .toList();

        int totalBreak = 0;
        int pairs = Math.min(breakStarts.size(), breakEnds.size());
        for (int i = 0; i < pairs; i++) {
            totalBreak += (int) java.time.Duration.between(breakStarts.get(i), breakEnds.get(i)).toMinutes();
        }
        return totalBreak;
    }

    private int calculateNightMinutes(OffsetDateTime clockIn, OffsetDateTime clockOut, LocalDate date) {
        LocalDateTime workStart = clockIn.toLocalDateTime();
        LocalDateTime workEnd = clockOut.toLocalDateTime();
        TimeRange workRange = TimeRange.of(workStart, workEnd);

        LocalDateTime nightStart = date.atTime(22, 0);
        LocalDateTime nightEnd = date.plusDays(1).atTime(5, 0);
        TimeRange nightRange = TimeRange.of(nightStart, nightEnd);

        return workRange.overlapMinutes(nightRange);
    }
}
