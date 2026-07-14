package com.example.attendance.timerecord;

import java.time.LocalDate;
import java.util.List;

import com.example.attendance.timerecord.dto.TimeRecordCreateRequest;
import com.example.attendance.timerecord.dto.TimeRecordResponse;

public interface TimeRecordService {

    TimeRecordResponse create(Long employeeId, TimeRecordCreateRequest request);

    List<TimeRecordResponse> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
}
