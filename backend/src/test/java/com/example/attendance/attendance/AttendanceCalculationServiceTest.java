package com.example.attendance.attendance;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.attendance.common.enums.AttendanceStatus;
import com.example.attendance.common.enums.DayType;
import com.example.attendance.common.enums.TimeRecordType;
import com.example.attendance.timerecord.TimeRecord;
import com.example.attendance.timerecord.TimeRecordRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceCalculationServiceTest {

    @Mock
    private TimeRecordRepository timeRecordRepository;

    @Mock
    private WorkCalendarRepository workCalendarRepository;

    @Mock
    private DailyAttendanceRepository dailyAttendanceRepository;

    private AttendanceCalculationServiceImpl service;

    private static final Long EMPLOYEE_ID = 1L;
    private static final LocalDate DATE = LocalDate.of(2026, 7, 14);
    private static final ZoneOffset JST = ZoneOffset.ofHours(9);

    @BeforeEach
    void setUp() {
        service = new AttendanceCalculationServiceImpl(
                timeRecordRepository, workCalendarRepository, dailyAttendanceRepository);
    }

    @Test
    @DisplayName("通常勤務: 9:00-17:30, 休憩60分 → 勤務450分, 残業0分")
    void calculate_normalWork_returnsCorrectDuration() {
        var records = List.of(
                buildRecord(TimeRecordType.CLOCK_IN, 9, 0),
                buildRecord(TimeRecordType.BREAK_START, 12, 0),
                buildRecord(TimeRecordType.BREAK_END, 13, 0),
                buildRecord(TimeRecordType.CLOCK_OUT, 17, 30));
        stubRecords(records);
        stubWorkday();

        var result = service.calculate(EMPLOYEE_ID, DATE);

        assertThat(result.getWorkingMinutes()).isEqualTo(450);
        assertThat(result.getOvertimeMinutes()).isEqualTo(0);
        assertThat(result.getBreakMinutes()).isEqualTo(60);
        assertThat(result.getNightMinutes()).isEqualTo(0);
        assertThat(result.isHolidayWork()).isFalse();
        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    @DisplayName("残業あり: 9:00-19:00, 休憩60分 → 勤務540分, 残業90分")
    void calculate_overtime_returnsOvertimeMinutes() {
        var records = List.of(
                buildRecord(TimeRecordType.CLOCK_IN, 9, 0),
                buildRecord(TimeRecordType.BREAK_START, 12, 0),
                buildRecord(TimeRecordType.BREAK_END, 13, 0),
                buildRecord(TimeRecordType.CLOCK_OUT, 19, 0));
        stubRecords(records);
        stubWorkday();

        var result = service.calculate(EMPLOYEE_ID, DATE);

        assertThat(result.getWorkingMinutes()).isEqualTo(540);
        assertThat(result.getOvertimeMinutes()).isEqualTo(90);
    }

    @Test
    @DisplayName("深夜勤務: 9:00-23:30, 休憩60分 → 深夜90分")
    void calculate_nightWork_returnsNightMinutes() {
        var records = List.of(
                buildRecord(TimeRecordType.CLOCK_IN, 9, 0),
                buildRecord(TimeRecordType.BREAK_START, 12, 0),
                buildRecord(TimeRecordType.BREAK_END, 13, 0),
                buildRecord(TimeRecordType.CLOCK_OUT, 23, 30));
        stubRecords(records);
        stubWorkday();

        var result = service.calculate(EMPLOYEE_ID, DATE);

        assertThat(result.getNightMinutes()).isEqualTo(90);
    }

    @Test
    @DisplayName("退勤未打刻: status=PRESENT, 時間は全て0")
    void calculate_noClockOut_returnsZeroDuration() {
        var records = List.of(
                buildRecord(TimeRecordType.CLOCK_IN, 9, 0));
        stubRecords(records);
        stubWorkday();

        var result = service.calculate(EMPLOYEE_ID, DATE);

        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(result.getWorkingMinutes()).isEqualTo(0);
        assertThat(result.getOvertimeMinutes()).isEqualTo(0);
        assertThat(result.getBreakMinutes()).isEqualTo(0);
        assertThat(result.getNightMinutes()).isEqualTo(0);
    }

    @Test
    @DisplayName("休日勤務: WorkCalendar が HOLIDAY → isHolidayWork=true")
    void calculate_holiday_setsHolidayWork() {
        var records = List.of(
                buildRecord(TimeRecordType.CLOCK_IN, 9, 0),
                buildRecord(TimeRecordType.CLOCK_OUT, 17, 30));
        stubRecords(records);
        stubHoliday();

        var result = service.calculate(EMPLOYEE_ID, DATE);

        assertThat(result.isHolidayWork()).isTrue();
        assertThat(result.getWorkingMinutes()).isEqualTo(510);
    }

    @Test
    @DisplayName("複数回休憩: 休憩時間が合算される")
    void calculate_multipleBreaks_sumsBreakMinutes() {
        var records = List.of(
                buildRecord(TimeRecordType.CLOCK_IN, 9, 0),
                buildRecord(TimeRecordType.BREAK_START, 12, 0),
                buildRecord(TimeRecordType.BREAK_END, 12, 45),
                buildRecord(TimeRecordType.BREAK_START, 15, 0),
                buildRecord(TimeRecordType.BREAK_END, 15, 15),
                buildRecord(TimeRecordType.CLOCK_OUT, 18, 0));
        stubRecords(records);
        stubWorkday();

        var result = service.calculate(EMPLOYEE_ID, DATE);

        assertThat(result.getBreakMinutes()).isEqualTo(60);
        assertThat(result.getWorkingMinutes()).isEqualTo(480);
    }

    @Test
    @DisplayName("打刻なし: status=ABSENT")
    void calculate_noRecords_returnsAbsent() {
        stubRecords(Collections.emptyList());
        stubWorkday();

        var result = service.calculate(EMPLOYEE_ID, DATE);

        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.ABSENT);
        assertThat(result.getWorkingMinutes()).isEqualTo(0);
    }

    @Test
    @DisplayName("打刻なし・休日: status=HOLIDAY")
    void calculate_noRecordsOnHoliday_returnsHoliday() {
        stubRecords(Collections.emptyList());
        stubHoliday();

        var result = service.calculate(EMPLOYEE_ID, DATE);

        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.HOLIDAY);
    }

    @Test
    @DisplayName("休憩未終了: 休憩開始のみで終了なしの場合、休憩0分として計算される")
    void calculate_openBreak_treatsBreakAsZero() {
        var records = List.of(
                buildRecord(TimeRecordType.CLOCK_IN, 9, 0),
                buildRecord(TimeRecordType.BREAK_START, 12, 0),
                buildRecord(TimeRecordType.CLOCK_OUT, 17, 30));
        stubRecords(records);
        stubWorkday();

        var result = service.calculate(EMPLOYEE_ID, DATE);

        assertThat(result.getBreakMinutes()).isEqualTo(0);
        assertThat(result.getWorkingMinutes()).isEqualTo(510);
    }

    @Test
    @DisplayName("出勤のみ・退勤なし: status=PRESENT, 時間0（計算保留）")
    void calculate_clockInOnly_pendingCalculation() {
        var records = List.of(
                buildRecord(TimeRecordType.CLOCK_IN, 9, 0),
                buildRecord(TimeRecordType.BREAK_START, 12, 0),
                buildRecord(TimeRecordType.BREAK_END, 13, 0));
        stubRecords(records);
        stubWorkday();

        var result = service.calculate(EMPLOYEE_ID, DATE);

        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(result.getWorkingMinutes()).isEqualTo(0);
        assertThat(result.getClockOut()).isNull();
    }

    private TimeRecord buildRecord(TimeRecordType type, int hour, int minute) {
        return TimeRecord.builder()
                .employeeId(EMPLOYEE_ID)
                .recordDate(DATE)
                .type(type)
                .recordedAt(OffsetDateTime.of(DATE.atTime(hour, minute), JST))
                .build();
    }

    private void stubRecords(List<TimeRecord> records) {
        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(EMPLOYEE_ID, DATE))
                .thenReturn(records);
    }

    private void stubWorkday() {
        when(workCalendarRepository.findByCalendarDate(DATE))
                .thenReturn(Optional.empty());
    }

    private void stubHoliday() {
        var calendar = WorkCalendar.builder()
                .calendarDate(DATE)
                .dayType(DayType.HOLIDAY)
                .fiscalYear(2026)
                .build();
        when(workCalendarRepository.findByCalendarDate(DATE))
                .thenReturn(Optional.of(calendar));
    }
}
