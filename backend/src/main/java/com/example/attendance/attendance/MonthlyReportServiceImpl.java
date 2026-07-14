package com.example.attendance.attendance;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.attendance.dto.DailyAttendanceResponse;
import com.example.attendance.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.attendance.dto.MonthlySummaryResponse;
import com.example.attendance.employee.EmployeeDepartment;
import com.example.attendance.employee.EmployeeDepartmentRepository;

@Service
@Transactional(readOnly = true)
public class MonthlyReportServiceImpl implements MonthlyReportService {

    private final DailyAttendanceRepository dailyAttendanceRepository;
    private final EmployeeDepartmentRepository employeeDepartmentRepository;

    public MonthlyReportServiceImpl(
            DailyAttendanceRepository dailyAttendanceRepository,
            EmployeeDepartmentRepository employeeDepartmentRepository) {
        this.dailyAttendanceRepository = dailyAttendanceRepository;
        this.employeeDepartmentRepository = employeeDepartmentRepository;
    }

    @Override
    public MonthlyAttendanceResponse getMonthlyAttendance(Long employeeId, int year, int month) {
        List<DailyAttendance> attendances = findMonthlyAttendances(employeeId, year, month);
        List<DailyAttendanceResponse> records = attendances.stream()
                .map(this::toResponse)
                .toList();
        MonthlySummaryResponse summary = buildSummary(employeeId, year, month, attendances);
        return new MonthlyAttendanceResponse(employeeId, year, month, records, summary);
    }

    @Override
    public MonthlySummaryResponse getMonthlySummary(Long employeeId, int year, int month) {
        List<DailyAttendance> attendances = findMonthlyAttendances(employeeId, year, month);
        return buildSummary(employeeId, year, month, attendances);
    }

    @Override
    public List<MonthlyAttendanceResponse> getDepartmentMonthlyAttendance(Long departmentId, int year, int month) {
        List<EmployeeDepartment> members = employeeDepartmentRepository
                .findByDepartmentIdAndEndDateIsNull(departmentId);
        List<Long> employeeIds = members.stream()
                .map(EmployeeDepartment::getEmployeeId)
                .toList();

        if (employeeIds.isEmpty()) {
            return List.of();
        }

        YearMonth ym = YearMonth.of(year, month);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<DailyAttendance> allAttendances = dailyAttendanceRepository
                .findByEmployeeIdsAndMonth(employeeIds, startDate, endDate);

        Map<Long, List<DailyAttendance>> grouped = allAttendances.stream()
                .collect(Collectors.groupingBy(DailyAttendance::getEmployeeId));

        return employeeIds.stream()
                .map(empId -> {
                    List<DailyAttendance> empAttendances = grouped.getOrDefault(empId, List.of());
                    List<DailyAttendanceResponse> records = empAttendances.stream()
                            .map(this::toResponse)
                            .toList();
                    MonthlySummaryResponse summary = buildSummary(empId, year, month, empAttendances);
                    return new MonthlyAttendanceResponse(empId, year, month, records, summary);
                })
                .toList();
    }

    private List<DailyAttendance> findMonthlyAttendances(Long employeeId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();
        return dailyAttendanceRepository.findByEmployeeIdAndMonth(employeeId, startDate, endDate);
    }

    private MonthlySummaryResponse buildSummary(Long employeeId, int year, int month,
                                                 List<DailyAttendance> attendances) {
        int totalWorkingMinutes = attendances.stream().mapToInt(DailyAttendance::getWorkingMinutes).sum();
        int totalOvertimeMinutes = attendances.stream().mapToInt(DailyAttendance::getOvertimeMinutes).sum();
        int totalNightMinutes = attendances.stream().mapToInt(DailyAttendance::getNightMinutes).sum();
        int totalHolidayWorkMinutes = attendances.stream()
                .filter(DailyAttendance::isHolidayWork)
                .mapToInt(DailyAttendance::getWorkingMinutes)
                .sum();
        int workingDays = (int) attendances.stream()
                .filter(a -> a.getWorkingMinutes() > 0)
                .count();

        return new MonthlySummaryResponse(
                employeeId, year, month,
                totalWorkingMinutes, totalOvertimeMinutes, totalNightMinutes,
                totalHolidayWorkMinutes, workingDays);
    }

    private DailyAttendanceResponse toResponse(DailyAttendance attendance) {
        return new DailyAttendanceResponse(
                attendance.getAttendanceDate(),
                attendance.getClockIn(),
                attendance.getClockOut(),
                attendance.getBreakMinutes(),
                attendance.getWorkingMinutes(),
                attendance.getOvertimeMinutes(),
                attendance.getNightMinutes(),
                attendance.isHolidayWork(),
                attendance.getStatus().name());
    }
}
