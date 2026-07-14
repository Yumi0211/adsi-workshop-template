package com.example.attendance.alert;

public interface OvertimeAlertService {

    void checkMonthlyOvertime(Long employeeId, int year, int month);

    void checkYearlyOvertime(Long employeeId, int fiscalYear);
}
