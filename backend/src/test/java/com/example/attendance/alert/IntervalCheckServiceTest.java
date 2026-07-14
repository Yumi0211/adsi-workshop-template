package com.example.attendance.alert;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.attendance.attendance.DailyAttendance;
import com.example.attendance.attendance.DailyAttendanceRepository;
import com.example.attendance.common.enums.AlertType;
import com.example.attendance.common.enums.AttendanceStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntervalCheckServiceTest {

    @Mock
    private DailyAttendanceRepository dailyAttendanceRepository;

    @Mock
    private AlertRepository alertRepository;

    private IntervalCheckServiceImpl service;

    private static final Long EMPLOYEE_ID = 1L;
    private static final ZoneOffset JST = ZoneOffset.ofHours(9);

    @BeforeEach
    void setUp() {
        service = new IntervalCheckServiceImpl(dailyAttendanceRepository, alertRepository);
    }

    @Test
    @DisplayName("勤務間インターバル11h未満: アラートが生成される")
    void checkInterval_lessThan11h_createsAlert() {
        var yesterday = LocalDate.of(2026, 7, 13);
        var today = LocalDate.of(2026, 7, 14);

        var yesterdayAttendance = DailyAttendance.builder()
                .employeeId(EMPLOYEE_ID)
                .attendanceDate(yesterday)
                .clockIn(yesterday.atTime(9, 0).atOffset(JST))
                .clockOut(yesterday.atTime(23, 0).atOffset(JST))
                .breakMinutes(60)
                .workingMinutes(780)
                .overtimeMinutes(300)
                .status(AttendanceStatus.PRESENT)
                .build();

        var todayAttendance = DailyAttendance.builder()
                .employeeId(EMPLOYEE_ID)
                .attendanceDate(today)
                .clockIn(today.atTime(8, 0).atOffset(JST))
                .breakMinutes(0)
                .workingMinutes(0)
                .overtimeMinutes(0)
                .status(AttendanceStatus.PRESENT)
                .build();

        when(dailyAttendanceRepository.findByEmployeeIdAndMonth(
                eq(EMPLOYEE_ID), eq(yesterday), eq(today)))
                .thenReturn(List.of(yesterdayAttendance, todayAttendance));
        when(alertRepository.existsByEmployeeIdAndTypeAndCreatedAtAfter(
                eq(EMPLOYEE_ID), eq(AlertType.INTERVAL_VIOLATION), any(OffsetDateTime.class)))
                .thenReturn(false);

        service.checkInterval(EMPLOYEE_ID, today);

        var captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(captor.capture());
        var alert = captor.getValue();
        assertThat(alert.getType()).isEqualTo(AlertType.INTERVAL_VIOLATION);
        assertThat(alert.getMessage()).contains("9時間");
    }

    @Test
    @DisplayName("勤務間インターバル11h以上: アラートなし")
    void checkInterval_11hOrMore_noAlert() {
        var yesterday = LocalDate.of(2026, 7, 13);
        var today = LocalDate.of(2026, 7, 14);

        var yesterdayAttendance = DailyAttendance.builder()
                .employeeId(EMPLOYEE_ID)
                .attendanceDate(yesterday)
                .clockIn(yesterday.atTime(9, 0).atOffset(JST))
                .clockOut(yesterday.atTime(18, 0).atOffset(JST))
                .breakMinutes(60)
                .workingMinutes(480)
                .overtimeMinutes(0)
                .status(AttendanceStatus.PRESENT)
                .build();

        var todayAttendance = DailyAttendance.builder()
                .employeeId(EMPLOYEE_ID)
                .attendanceDate(today)
                .clockIn(today.atTime(9, 0).atOffset(JST))
                .breakMinutes(0)
                .workingMinutes(0)
                .overtimeMinutes(0)
                .status(AttendanceStatus.PRESENT)
                .build();

        when(dailyAttendanceRepository.findByEmployeeIdAndMonth(
                eq(EMPLOYEE_ID), eq(yesterday), eq(today)))
                .thenReturn(List.of(yesterdayAttendance, todayAttendance));

        service.checkInterval(EMPLOYEE_ID, today);

        verify(alertRepository, never()).save(any());
    }

    @Test
    @DisplayName("重複アラートは生成されない")
    void checkInterval_duplicate_notCreated() {
        var yesterday = LocalDate.of(2026, 7, 13);
        var today = LocalDate.of(2026, 7, 14);

        var yesterdayAttendance = DailyAttendance.builder()
                .employeeId(EMPLOYEE_ID)
                .attendanceDate(yesterday)
                .clockIn(yesterday.atTime(9, 0).atOffset(JST))
                .clockOut(yesterday.atTime(23, 0).atOffset(JST))
                .breakMinutes(60)
                .workingMinutes(780)
                .overtimeMinutes(300)
                .status(AttendanceStatus.PRESENT)
                .build();

        var todayAttendance = DailyAttendance.builder()
                .employeeId(EMPLOYEE_ID)
                .attendanceDate(today)
                .clockIn(today.atTime(8, 0).atOffset(JST))
                .breakMinutes(0)
                .workingMinutes(0)
                .overtimeMinutes(0)
                .status(AttendanceStatus.PRESENT)
                .build();

        when(dailyAttendanceRepository.findByEmployeeIdAndMonth(
                eq(EMPLOYEE_ID), eq(yesterday), eq(today)))
                .thenReturn(List.of(yesterdayAttendance, todayAttendance));
        when(alertRepository.existsByEmployeeIdAndTypeAndCreatedAtAfter(
                eq(EMPLOYEE_ID), eq(AlertType.INTERVAL_VIOLATION), any(OffsetDateTime.class)))
                .thenReturn(true);

        service.checkInterval(EMPLOYEE_ID, today);

        verify(alertRepository, never()).save(any());
    }

    @Test
    @DisplayName("前日の退勤がない場合: アラートなし")
    void checkInterval_noClockOutYesterday_noAlert() {
        var yesterday = LocalDate.of(2026, 7, 13);
        var today = LocalDate.of(2026, 7, 14);

        var yesterdayAttendance = DailyAttendance.builder()
                .employeeId(EMPLOYEE_ID)
                .attendanceDate(yesterday)
                .clockIn(yesterday.atTime(9, 0).atOffset(JST))
                .clockOut(null)
                .breakMinutes(0)
                .workingMinutes(0)
                .overtimeMinutes(0)
                .status(AttendanceStatus.PRESENT)
                .build();

        var todayAttendance = DailyAttendance.builder()
                .employeeId(EMPLOYEE_ID)
                .attendanceDate(today)
                .clockIn(today.atTime(9, 0).atOffset(JST))
                .breakMinutes(0)
                .workingMinutes(0)
                .overtimeMinutes(0)
                .status(AttendanceStatus.PRESENT)
                .build();

        when(dailyAttendanceRepository.findByEmployeeIdAndMonth(
                eq(EMPLOYEE_ID), eq(yesterday), eq(today)))
                .thenReturn(List.of(yesterdayAttendance, todayAttendance));

        service.checkInterval(EMPLOYEE_ID, today);

        verify(alertRepository, never()).save(any());
    }
}
