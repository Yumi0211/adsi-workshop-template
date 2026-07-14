package com.example.attendance.timerecord;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.common.enums.TimeRecordType;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.timerecord.dto.TimeRecordCreateRequest;
import com.example.attendance.timerecord.dto.TimeRecordResponse;

@Service
@Transactional
public class TimeRecordServiceImpl implements TimeRecordService {

    private final TimeRecordRepository timeRecordRepository;

    public TimeRecordServiceImpl(TimeRecordRepository timeRecordRepository) {
        this.timeRecordRepository = timeRecordRepository;
    }

    @Override
    public TimeRecordResponse create(Long employeeId, TimeRecordCreateRequest request) {
        LocalDate today = LocalDate.now();
        List<TimeRecord> todayRecords = timeRecordRepository
                .findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(employeeId, today);

        validateRecordOrder(request.type(), todayRecords);

        var timeRecord = TimeRecord.builder()
                .employeeId(employeeId)
                .recordDate(today)
                .type(request.type())
                .recordedAt(OffsetDateTime.now())
                .source(request.source())
                .build();

        var saved = timeRecordRepository.save(timeRecord);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeRecordResponse> findByEmployeeIdAndDate(Long employeeId, LocalDate date) {
        return timeRecordRepository
                .findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(employeeId, date)
                .stream()
                .map(this::toResponse)
                .toList();
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
            case CLOCK_IN -> {
                if (hasClockedIn) {
                    throw new BusinessException("既に出勤打刻済みです。同日に2回の出勤打刻はできません");
                }
            }
            case CLOCK_OUT -> {
                if (!hasClockedIn) {
                    throw new BusinessException("出勤打刻がありません。先に出勤打刻をしてください");
                }
            }
            case BREAK_START -> {
                if (!hasClockedIn) {
                    throw new BusinessException("出勤打刻がありません。先に出勤打刻をしてください");
                }
                boolean hasOpenBreak = todayRecords.stream()
                        .filter(r -> r.getType() == TimeRecordType.BREAK_START)
                        .count() > todayRecords.stream()
                        .filter(r -> r.getType() == TimeRecordType.BREAK_END)
                        .count();
                if (hasOpenBreak) {
                    throw new BusinessException("休憩終了していません。先に休憩終了を打刻してください");
                }
            }
            case BREAK_END -> {
                if (!hasClockedIn) {
                    throw new BusinessException("出勤打刻がありません。先に出勤打刻をしてください");
                }
                boolean hasOpenBreak = todayRecords.stream()
                        .filter(r -> r.getType() == TimeRecordType.BREAK_START)
                        .count() > todayRecords.stream()
                        .filter(r -> r.getType() == TimeRecordType.BREAK_END)
                        .count();
                if (!hasOpenBreak) {
                    throw new BusinessException("休憩開始していません。先に休憩開始を打刻してください");
                }
            }
        }
    }

    private TimeRecordResponse toResponse(TimeRecord record) {
        return new TimeRecordResponse(
                record.getId(),
                record.getEmployeeId(),
                record.getRecordDate(),
                record.getType(),
                record.getRecordedAt(),
                record.getSource()
        );
    }
}
