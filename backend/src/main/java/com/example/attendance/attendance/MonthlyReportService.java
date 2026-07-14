package com.example.attendance.attendance;

import java.util.List;

import com.example.attendance.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.attendance.dto.MonthlySummaryResponse;

public interface MonthlyReportService {

    MonthlyAttendanceResponse getMonthlyAttendance(Long employeeId, int year, int month);

    MonthlySummaryResponse getMonthlySummary(Long employeeId, int year, int month);

    List<MonthlyAttendanceResponse> getDepartmentMonthlyAttendance(Long departmentId, int year, int month);
}
