package com.example.attendance.timerecord;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.common.enums.TimeRecordType;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.employee.EmployeeRepository;
import com.example.attendance.timerecord.dto.TimeRecordCreateRequest;
import com.example.attendance.timerecord.dto.TimeRecordResponse;

@Service
@Transactional
public class TimeRecordServiceImpl implements TimeRecordService {

    private final TimeRecordRepository timeRecordRepository;
    private final EmployeeRepository employeeRepository;

    public TimeRecordServiceImpl(TimeRecordRepository timeRecordRepository, EmployeeRepository employeeRepository) {
        this.timeRecordRepository = timeRecordRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public TimeRecordResponse create(String email, TimeRecordCreateRequest request) {
        Long employeeId = resolveEmployeeId(email);
        OffsetDateTime now = OffsetDateTime.now();
        LocalDate today = now.toLocalDate();

        List<TimeRecord> todayRecords = timeRecordRepository
                .findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(employeeId, today);

        validateRecordOrder(request.type(), todayRecords);

        var timeRecord = TimeRecord.builder()
                .employeeId(employeeId)
                .recordDate(today)
                .type(request.type())
                .recordedAt(now)
                .source(request.source())
                .build();

        var saved = timeRecordRepository.save(timeRecord);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeRecordResponse> findByDate(String email, LocalDate date) {
        Long employeeId = resolveEmployeeId(email);
        return timeRecordRepository
                .findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(employeeId, date)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Long resolveEmployeeId(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", email))
                .getId();
    }

    private void validateRecordOrder(TimeRecordType type, List<TimeRecord> todayRecords) {
        boolean hasClockedOut = todayRecords.stream()
                .anyMatch(r -> r.getType() == TimeRecordType.CLOCK_OUT);

        if (hasClockedOut) {
            throw new BusinessException("既に退勤済みです。退勤後の追加打刻はできません");
        }

        boolean hasClockedIn = todayRecords.stream()
                .anyMatch(r -> r.getType() == TimeRecordType.CLOCK_IN);

        switch (type) {
            case CLOCK_IN -> validateClockIn(hasClockedIn);
            case CLOCK_OUT -> validateClockOut(hasClockedIn);
            case BREAK_START -> validateBreakStart(hasClockedIn, todayRecords);
            case BREAK_END -> validateBreakEnd(hasClockedIn, todayRecords);
        }
    }

    private void validateClockIn(boolean hasClockedIn) {
        if (hasClockedIn) {
            throw new BusinessException("既に出勤打刻済みです。同日に2回の出勤打刻はできません");
        }
    }

    private void validateClockOut(boolean hasClockedIn) {
        if (!hasClockedIn) {
            throw new BusinessException("出勤打刻がありません。先に出勤打刻をしてください");
        }
    }

    private void validateBreakStart(boolean hasClockedIn, List<TimeRecord> todayRecords) {
        if (!hasClockedIn) {
            throw new BusinessException("出勤打刻がありません。先に出勤打刻をしてください");
        }
        if (hasOpenBreak(todayRecords)) {
            throw new BusinessException("休憩終了していません。先に休憩終了を打刻してください");
        }
    }

    private void validateBreakEnd(boolean hasClockedIn, List<TimeRecord> todayRecords) {
        if (!hasClockedIn) {
            throw new BusinessException("出勤打刻がありません。先に出勤打刻をしてください");
        }
        if (!hasOpenBreak(todayRecords)) {
            throw new BusinessException("休憩開始していません。先に休憩開始を打刻してください");
        }
    }

    private boolean hasOpenBreak(List<TimeRecord> todayRecords) {
        long breakStartCount = todayRecords.stream()
                .filter(r -> r.getType() == TimeRecordType.BREAK_START)
                .count();
        long breakEndCount = todayRecords.stream()
                .filter(r -> r.getType() == TimeRecordType.BREAK_END)
                .count();
        return breakStartCount > breakEndCount;
    }

    private TimeRecordResponse toResponse(TimeRecord record) {
        return new TimeRecordResponse(
                record.getId(),
                record.getRecordDate(),
                record.getType(),
                record.getRecordedAt(),
                record.getSource()
        );
    }
}
