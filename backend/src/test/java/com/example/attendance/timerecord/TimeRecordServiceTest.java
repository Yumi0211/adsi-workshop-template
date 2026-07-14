package com.example.attendance.timerecord;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.attendance.common.enums.RecordSource;
import com.example.attendance.common.enums.TimeRecordType;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.timerecord.dto.TimeRecordCreateRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeRecordServiceTest {

    @Mock
    private TimeRecordRepository timeRecordRepository;

    private TimeRecordServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TimeRecordServiceImpl(timeRecordRepository);
    }

    @Test
    @DisplayName("当日未打刻の出勤打刻が記録される")
    void clockIn_firstRecordOfDay_createsTimeRecord() {
        var request = new TimeRecordCreateRequest(TimeRecordType.CLOCK_IN, RecordSource.WEB, null);
        Long employeeId = 1L;

        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(any(), any()))
                .thenReturn(Collections.emptyList());
        when(timeRecordRepository.save(any(TimeRecord.class))).thenAnswer(inv -> {
            var record = inv.getArgument(0, TimeRecord.class);
            record.setId(1L);
            return record;
        });

        var result = service.create(employeeId, request);

        assertThat(result.type()).isEqualTo(TimeRecordType.CLOCK_IN);
        assertThat(result.employeeId()).isEqualTo(employeeId);
        assertThat(result.source()).isEqualTo(RecordSource.WEB);
    }

    @Test
    @DisplayName("二重出勤打刻が防止される")
    void clockIn_alreadyClockedIn_throwsBusinessException() {
        var request = new TimeRecordCreateRequest(TimeRecordType.CLOCK_IN, RecordSource.WEB, null);
        Long employeeId = 1L;

        var existingRecord = TimeRecord.builder()
                .id(1L).employeeId(employeeId).recordDate(LocalDate.now())
                .type(TimeRecordType.CLOCK_IN).recordedAt(OffsetDateTime.now())
                .source(RecordSource.WEB).build();

        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(any(), any()))
                .thenReturn(List.of(existingRecord));

        assertThatThrownBy(() -> service.create(employeeId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("出勤");
    }

    @Test
    @DisplayName("出勤後の退勤打刻が記録される")
    void clockOut_afterClockIn_createsTimeRecord() {
        var request = new TimeRecordCreateRequest(TimeRecordType.CLOCK_OUT, RecordSource.WEB, null);
        Long employeeId = 1L;

        var clockInRecord = TimeRecord.builder()
                .id(1L).employeeId(employeeId).recordDate(LocalDate.now())
                .type(TimeRecordType.CLOCK_IN).recordedAt(OffsetDateTime.now())
                .source(RecordSource.WEB).build();

        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(any(), any()))
                .thenReturn(List.of(clockInRecord));
        when(timeRecordRepository.save(any(TimeRecord.class))).thenAnswer(inv -> {
            var record = inv.getArgument(0, TimeRecord.class);
            record.setId(2L);
            return record;
        });

        var result = service.create(employeeId, request);

        assertThat(result.type()).isEqualTo(TimeRecordType.CLOCK_OUT);
    }

    @Test
    @DisplayName("出勤前の退勤打刻はエラー")
    void clockOut_withoutClockIn_throwsBusinessException() {
        var request = new TimeRecordCreateRequest(TimeRecordType.CLOCK_OUT, RecordSource.WEB, null);
        Long employeeId = 1L;

        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(any(), any()))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> service.create(employeeId, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("退勤済みの日に追加打刻はエラー")
    void clockOut_alreadyClockedOut_throwsBusinessException() {
        var request = new TimeRecordCreateRequest(TimeRecordType.CLOCK_IN, RecordSource.WEB, null);
        Long employeeId = 1L;

        var clockIn = TimeRecord.builder()
                .id(1L).employeeId(employeeId).recordDate(LocalDate.now())
                .type(TimeRecordType.CLOCK_IN).recordedAt(OffsetDateTime.now())
                .source(RecordSource.WEB).build();
        var clockOut = TimeRecord.builder()
                .id(2L).employeeId(employeeId).recordDate(LocalDate.now())
                .type(TimeRecordType.CLOCK_OUT).recordedAt(OffsetDateTime.now().plusHours(8))
                .source(RecordSource.WEB).build();

        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(any(), any()))
                .thenReturn(List.of(clockIn, clockOut));

        assertThatThrownBy(() -> service.create(employeeId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("退勤");
    }

    @Test
    @DisplayName("出勤後の休憩開始が記録される")
    void breakStart_afterClockIn_createsTimeRecord() {
        var request = new TimeRecordCreateRequest(TimeRecordType.BREAK_START, RecordSource.WEB, null);
        Long employeeId = 1L;

        var clockIn = TimeRecord.builder()
                .id(1L).employeeId(employeeId).recordDate(LocalDate.now())
                .type(TimeRecordType.CLOCK_IN).recordedAt(OffsetDateTime.now())
                .source(RecordSource.WEB).build();

        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(any(), any()))
                .thenReturn(List.of(clockIn));
        when(timeRecordRepository.save(any(TimeRecord.class))).thenAnswer(inv -> {
            var record = inv.getArgument(0, TimeRecord.class);
            record.setId(2L);
            return record;
        });

        var result = service.create(employeeId, request);

        assertThat(result.type()).isEqualTo(TimeRecordType.BREAK_START);
    }

    @Test
    @DisplayName("休憩開始後の休憩終了が記録される")
    void breakEnd_afterBreakStart_createsTimeRecord() {
        var request = new TimeRecordCreateRequest(TimeRecordType.BREAK_END, RecordSource.WEB, null);
        Long employeeId = 1L;

        var clockIn = TimeRecord.builder()
                .id(1L).employeeId(employeeId).recordDate(LocalDate.now())
                .type(TimeRecordType.CLOCK_IN).recordedAt(OffsetDateTime.now())
                .source(RecordSource.WEB).build();
        var breakStart = TimeRecord.builder()
                .id(2L).employeeId(employeeId).recordDate(LocalDate.now())
                .type(TimeRecordType.BREAK_START).recordedAt(OffsetDateTime.now().plusHours(3))
                .source(RecordSource.WEB).build();

        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(any(), any()))
                .thenReturn(List.of(clockIn, breakStart));
        when(timeRecordRepository.save(any(TimeRecord.class))).thenAnswer(inv -> {
            var record = inv.getArgument(0, TimeRecord.class);
            record.setId(3L);
            return record;
        });

        var result = service.create(employeeId, request);

        assertThat(result.type()).isEqualTo(TimeRecordType.BREAK_END);
    }

    @Test
    @DisplayName("出勤前の休憩開始はエラー")
    void breakStart_withoutClockIn_throwsBusinessException() {
        var request = new TimeRecordCreateRequest(TimeRecordType.BREAK_START, RecordSource.WEB, null);
        Long employeeId = 1L;

        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(any(), any()))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> service.create(employeeId, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("指定日の打刻一覧が取得できる")
    void findByDate_returnsRecordsForDay() {
        Long employeeId = 1L;
        LocalDate date = LocalDate.of(2026, 7, 14);

        var clockIn = TimeRecord.builder()
                .id(1L).employeeId(employeeId).recordDate(date)
                .type(TimeRecordType.CLOCK_IN).recordedAt(OffsetDateTime.now())
                .source(RecordSource.WEB).build();

        when(timeRecordRepository.findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(employeeId, date))
                .thenReturn(List.of(clockIn));

        var result = service.findByEmployeeIdAndDate(employeeId, date);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(TimeRecordType.CLOCK_IN);
    }
}
