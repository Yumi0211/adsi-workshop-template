package com.example.attendance.alert;

import java.time.LocalDate;

public interface IntervalCheckService {

    void checkInterval(Long employeeId, LocalDate date);
}
