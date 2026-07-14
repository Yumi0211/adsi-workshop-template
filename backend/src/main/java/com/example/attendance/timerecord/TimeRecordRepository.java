package com.example.attendance.timerecord;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeRecordRepository extends JpaRepository<TimeRecord, Long> {

    List<TimeRecord> findByEmployeeIdAndRecordDateOrderByRecordedAtAsc(Long employeeId, LocalDate recordDate);
}
