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
class OvertimeAlertServiceTest {

    @Mock
    private DailyAttendanceRepository dailyAttendanceRepository;

    @Mock
    private AlertRepository alertRepository;

    private OvertimeAlertServiceImpl service;

    private static final Long EMPLOYEE_ID = 1L;
    private static final ZoneOffset JST = ZoneOffset.ofHours(9);

    @BeforeEach
    void setUp() {
        service = new OvertimeAlertServiceImpl(dailyAttendanceRepository, alertRepository);
    }

    @Test
    @DisplayName("月45h超過: アラートが生成される")
    void checkMonthlyOvertime_over45h_createsAlert() {
        var attendances = List.of(
                buildAttendance(LocalDate.of(2026, 7, 1), 600),
                buildAttendance(LocalDate.of(2026, 7, 2), 600),
                buildAttendance(LocalDate.of(2026, 7, 3), 600),
                buildAttendance(LocalDate.of(2026, 7, 4), 600),
                buildAttendance(LocalDate.of(2026, 7, 5), 600)
        );

        when(dailyAttendanceRepository.findByEmployeeIdAndMonth(
                eq(EMPLOYEE_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(attendances);
        when(alertRepository.existsByEmployeeIdAndTypeAndCreatedAtAfter(
                eq(EMPLOYEE_ID), eq(AlertType.OVERTIME_MONTHLY), any(OffsetDateTime.class)))
                .thenReturn(false);

        service.checkMonthlyOvertime(EMPLOYEE_ID, 2026, 7);

        var captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(captor.capture());
        var alert = captor.getValue();
        assertThat(alert.getEmployeeId()).isEqualTo(EMPLOYEE_ID);
        assertThat(alert.getType()).isEqualTo(AlertType.OVERTIME_MONTHLY);
    }

    @Test
    @DisplayName("月45h以下: アラートが生成されない")
    void checkMonthlyOvertime_under45h_noAlert() {
        var attendances = List.of(
                buildAttendance(LocalDate.of(2026, 7, 1), 60),
                buildAttendance(LocalDate.of(2026, 7, 2), 60)
        );

        when(dailyAttendanceRepository.findByEmployeeIdAndMonth(
                eq(EMPLOYEE_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(attendances);

        service.checkMonthlyOvertime(EMPLOYEE_ID, 2026, 7);

        verify(alertRepository, never()).save(any());
    }

    @Test
    @DisplayName("月45h超過でも重複アラートは生成されない")
    void checkMonthlyOvertime_duplicateAlert_notCreated() {
        var attendances = List.of(
                buildAttendance(LocalDate.of(2026, 7, 1), 2701)
        );

        when(dailyAttendanceRepository.findByEmployeeIdAndMonth(
                eq(EMPLOYEE_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(attendances);
        when(alertRepository.existsByEmployeeIdAndTypeAndCreatedAtAfter(
                eq(EMPLOYEE_ID), eq(AlertType.OVERTIME_MONTHLY), any(OffsetDateTime.class)))
                .thenReturn(true);

        service.checkMonthlyOvertime(EMPLOYEE_ID, 2026, 7);

        verify(alertRepository, never()).save(any());
    }

    @Test
    @DisplayName("年360h超過: アラートが生成される")
    void checkYearlyOvertime_over360h_createsAlert() {
        var attendances = List.of(
                buildAttendance(LocalDate.of(2026, 4, 1), 21601)
        );

        when(dailyAttendanceRepository.findByEmployeeIdAndMonth(
                eq(EMPLOYEE_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(attendances);
        when(alertRepository.existsByEmployeeIdAndTypeAndCreatedAtAfter(
                eq(EMPLOYEE_ID), eq(AlertType.OVERTIME_YEARLY), any(OffsetDateTime.class)))
                .thenReturn(false);

        service.checkYearlyOvertime(EMPLOYEE_ID, 2026);

        var captor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(AlertType.OVERTIME_YEARLY);
    }

    @Test
    @DisplayName("年360h以下: アラートが生成されない")
    void checkYearlyOvertime_under360h_noAlert() {
        var attendances = List.of(
                buildAttendance(LocalDate.of(2026, 4, 1), 60)
        );

        when(dailyAttendanceRepository.findByEmployeeIdAndMonth(
                eq(EMPLOYEE_ID), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(attendances);

        service.checkYearlyOvertime(EMPLOYEE_ID, 2026);

        verify(alertRepository, never()).save(any());
    }

    private DailyAttendance buildAttendance(LocalDate date, int overtimeMinutes) {
        return DailyAttendance.builder()
                .employeeId(EMPLOYEE_ID)
                .attendanceDate(date)
                .clockIn(date.atTime(9, 0).atOffset(JST))
                .clockOut(date.atTime(18, 0).atOffset(JST))
                .breakMinutes(60)
                .workingMinutes(480 + overtimeMinutes)
                .overtimeMinutes(overtimeMinutes)
                .nightMinutes(0)
                .holidayWork(false)
                .status(AttendanceStatus.PRESENT)
                .build();
    }
}
