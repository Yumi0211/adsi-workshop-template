package com.example.attendance.attendance;

import java.time.LocalDate;

public interface AttendanceCalculationService {

    DailyAttendance calculate(Long employeeId, LocalDate date);
}
