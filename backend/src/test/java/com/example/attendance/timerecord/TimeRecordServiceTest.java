package com.example.attendance.timerecord;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.attendance.common.enums.RecordSource;
import com.example.attendance.common.enums.TimeRecordType;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.EmployeeRepository;
import com.example.attendance.timerecord.dto.TimeRecordCreateRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeRecordServiceTest {

    @Mock
    private TimeRecordRepository timeRecordRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private TimeRecordServiceImpl service;

    private static final String EMAIL = "tanaka@example.com";
    private static final Long EMPLOYEE_ID = 1L;

    @BeforeEach
    void setUp() {
        service = new TimeRecordServiceImpl(timeRecordRepository, employeeRepository);
    }


    private void stubEmployeeLookup() {
        when(employeeRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(Employee.builder().id(EMPLOYEE_ID).email(EMAIL).build()));
    }

    @Test
    @DisplayName("当日未打刻の出勤打刻が記録される")
    void clockIn_firstRecordOfDay_createsTimeRecord() {
        stubEmployeeLookup();
        var request = new TimeRecordCreateRequest(TimeRecordType.CLOCK_IN, RecordSource.WEB, null);

        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(eq(EMPLOYEE_ID), any()))
                .thenReturn(Collections.emptyList());
        when(timeRecordRepository.save(any(TimeRecord.class))).thenAnswer(inv -> {
            var record = inv.getArgument(0, TimeRecord.class);
            record.setId(1L);
            return record;
        });

        var result = service.create(EMAIL, request);

        assertThat(result.type()).isEqualTo(TimeRecordType.CLOCK_IN);
        assertThat(result.source()).isEqualTo(RecordSource.WEB);
    }

    @Test
    @DisplayName("二重出勤打刻が防止される")
    void clockIn_alreadyClockedIn_throwsBusinessException() {
        stubEmployeeLookup();
        var request = new TimeRecordCreateRequest(TimeRecordType.CLOCK_IN, RecordSource.WEB, null);

        var existingRecord = TimeRecord.builder()
                .id(1L).employeeId(EMPLOYEE_ID).recordDate(LocalDate.now())
                .type(TimeRecordType.CLOCK_IN).recordedAt(OffsetDateTime.now())
                .source(RecordSource.WEB).build();

        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(eq(EMPLOYEE_ID), any()))
                .thenReturn(List.of(existingRecord));

        assertThatThrownBy(() -> service.create(EMAIL, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("出勤");
    }

    @Test
    @DisplayName("出勤後の退勤打刻が記録される")
    void clockOut_afterClockIn_createsTimeRecord() {
        stubEmployeeLookup();
        var request = new TimeRecordCreateRequest(TimeRecordType.CLOCK_OUT, RecordSource.WEB, null);

        var clockInRecord = TimeRecord.builder()
                .id(1L).employeeId(EMPLOYEE_ID).recordDate(LocalDate.now())
                .type(TimeRecordType.CLOCK_IN).recordedAt(OffsetDateTime.now())
                .source(RecordSource.WEB).build();

        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(eq(EMPLOYEE_ID), any()))
                .thenReturn(List.of(clockInRecord));
        when(timeRecordRepository.save(any(TimeRecord.class))).thenAnswer(inv -> {
            var record = inv.getArgument(0, TimeRecord.class);
            record.setId(2L);
            return record;
        });

        var result = service.create(EMAIL, request);

        assertThat(result.type()).isEqualTo(TimeRecordType.CLOCK_OUT);
    }

    @Test
    @DisplayName("出勤前の退勤打刻はエラー")
    void clockOut_withoutClockIn_throwsBusinessException() {
        stubEmployeeLookup();
        var request = new TimeRecordCreateRequest(TimeRecordType.CLOCK_OUT, RecordSource.WEB, null);

        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(eq(EMPLOYEE_ID), any()))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> service.create(EMAIL, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("退勤済みの日にはいかなる打刻もエラー")
    void anyType_afterClockOut_throwsBusinessException() {
        stubEmployeeLookup();
        var request = new TimeRecordCreateRequest(TimeRecordType.BREAK_START, RecordSource.WEB, null);

        var clockIn = TimeRecord.builder()
                .id(1L).employeeId(EMPLOYEE_ID).recordDate(LocalDate.now())
                .type(TimeRecordType.CLOCK_IN).recordedAt(OffsetDateTime.now())
                .source(RecordSource.WEB).build();
        var clockOut = TimeRecord.builder()
                .id(2L).employeeId(EMPLOYEE_ID).recordDate(LocalDate.now())
                .type(TimeRecordType.CLOCK_OUT).recordedAt(OffsetDateTime.now().plusHours(8))
                .source(RecordSource.WEB).build();

        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(eq(EMPLOYEE_ID), any()))
                .thenReturn(List.of(clockIn, clockOut));

        assertThatThrownBy(() -> service.create(EMAIL, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("退勤");
    }

    @Test
    @DisplayName("出勤後の休憩開始が記録される")
    void breakStart_afterClockIn_createsTimeRecord() {
        stubEmployeeLookup();
        var request = new TimeRecordCreateRequest(TimeRecordType.BREAK_START, RecordSource.WEB, null);

        var clockIn = TimeRecord.builder()
                .id(1L).employeeId(EMPLOYEE_ID).recordDate(LocalDate.now())
                .type(TimeRecordType.CLOCK_IN).recordedAt(OffsetDateTime.now())
                .source(RecordSource.WEB).build();

        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(eq(EMPLOYEE_ID), any()))
                .thenReturn(List.of(clockIn));
        when(timeRecordRepository.save(any(TimeRecord.class))).thenAnswer(inv -> {
            var record = inv.getArgument(0, TimeRecord.class);
            record.setId(2L);
            return record;
        });

        var result = service.create(EMAIL, request);

        assertThat(result.type()).isEqualTo(TimeRecordType.BREAK_START);
    }

    @Test
    @DisplayName("休憩中に再度休憩開始はエラー")
    void breakStart_duringBreak_throwsBusinessException() {
        stubEmployeeLookup();
        var request = new TimeRecordCreateRequest(TimeRecordType.BREAK_START, RecordSource.WEB, null);

        var clockIn = TimeRecord.builder()
                .id(1L).employeeId(EMPLOYEE_ID).recordDate(LocalDate.now())
                .type(TimeRecordType.CLOCK_IN).recordedAt(OffsetDateTime.now())
                .source(RecordSource.WEB).build();
        var breakStart = TimeRecord.builder()
                .id(2L).employeeId(EMPLOYEE_ID).recordDate(LocalDate.now())
                .type(TimeRecordType.BREAK_START).recordedAt(OffsetDateTime.now().plusHours(3))
                .source(RecordSource.WEB).build();

        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(eq(EMPLOYEE_ID), any()))
                .thenReturn(List.of(clockIn, breakStart));

        assertThatThrownBy(() -> service.create(EMAIL, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("休憩終了");
    }

    @Test
    @DisplayName("休憩開始後の休憩終了が記録される")
    void breakEnd_afterBreakStart_createsTimeRecord() {
        stubEmployeeLookup();
        var request = new TimeRecordCreateRequest(TimeRecordType.BREAK_END, RecordSource.WEB, null);

        var clockIn = TimeRecord.builder()
                .id(1L).employeeId(EMPLOYEE_ID).recordDate(LocalDate.now())
                .type(TimeRecordType.CLOCK_IN).recordedAt(OffsetDateTime.now())
                .source(RecordSource.WEB).build();
        var breakStart = TimeRecord.builder()
                .id(2L).employeeId(EMPLOYEE_ID).recordDate(LocalDate.now())
                .type(TimeRecordType.BREAK_START).recordedAt(OffsetDateTime.now().plusHours(3))
                .source(RecordSource.WEB).build();

        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(eq(EMPLOYEE_ID), any()))
                .thenReturn(List.of(clockIn, breakStart));
        when(timeRecordRepository.save(any(TimeRecord.class))).thenAnswer(inv -> {
            var record = inv.getArgument(0, TimeRecord.class);
            record.setId(3L);
            return record;
        });

        var result = service.create(EMAIL, request);

        assertThat(result.type()).isEqualTo(TimeRecordType.BREAK_END);
    }

    @Test
    @DisplayName("休憩開始なしの休憩終了はエラー")
    void breakEnd_withoutBreakStart_throwsBusinessException() {
        stubEmployeeLookup();
        var request = new TimeRecordCreateRequest(TimeRecordType.BREAK_END, RecordSource.WEB, null);

        var clockIn = TimeRecord.builder()
                .id(1L).employeeId(EMPLOYEE_ID).recordDate(LocalDate.now())
                .type(TimeRecordType.CLOCK_IN).recordedAt(OffsetDateTime.now())
                .source(RecordSource.WEB).build();

        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(eq(EMPLOYEE_ID), any()))
                .thenReturn(List.of(clockIn));

        assertThatThrownBy(() -> service.create(EMAIL, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("休憩開始");
    }

    @Test
    @DisplayName("出勤前の休憩開始はエラー")
    void breakStart_withoutClockIn_throwsBusinessException() {
        stubEmployeeLookup();
        var request = new TimeRecordCreateRequest(TimeRecordType.BREAK_START, RecordSource.WEB, null);

        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(eq(EMPLOYEE_ID), any()))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> service.create(EMAIL, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("指定日の打刻一覧が取得できる")
    void findByDate_returnsRecordsForDay() {
        stubEmployeeLookup();
        LocalDate date = LocalDate.of(2026, 7, 14);

        var clockIn = TimeRecord.builder()
                .id(1L).employeeId(EMPLOYEE_ID).recordDate(date)
                .type(TimeRecordType.CLOCK_IN).recordedAt(OffsetDateTime.now())
                .source(RecordSource.WEB).build();

        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(EMPLOYEE_ID, date))
                .thenReturn(List.of(clockIn));

        var result = service.findByDate(EMAIL, date);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(TimeRecordType.CLOCK_IN);
    }

    @Test
    @DisplayName("打刻データなしの日は空リストが返る")
    void findByDate_noRecords_returnsEmptyList() {
        stubEmployeeLookup();
        LocalDate date = LocalDate.of(2026, 7, 14);

        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(EMPLOYEE_ID, date))
                .thenReturn(Collections.emptyList());

        var result = service.findByDate(EMAIL, date);

        assertThat(result).isEmpty();
    }
}
