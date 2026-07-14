package com.example.attendance.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.admin.dto.LeaveObligationRecordResponse;
import com.example.attendance.admin.dto.LeaveObligationResponse;
import com.example.attendance.admin.dto.OvertimeRecordResponse;
import com.example.attendance.admin.dto.OvertimeReportResponse;
import com.example.attendance.attendance.DailyAttendance;
import com.example.attendance.attendance.DailyAttendanceRepository;
import com.example.attendance.employee.Department;
import com.example.attendance.employee.DepartmentRepository;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.EmployeeDepartment;
import com.example.attendance.employee.EmployeeDepartmentRepository;
import com.example.attendance.employee.EmployeeRepository;
import com.example.attendance.leave.LeaveBalance;
import com.example.attendance.leave.LeaveBalanceRepository;

@Service
@Transactional(readOnly = true)
public class AdminReportServiceImpl implements AdminReportService {

    private static final int MONTHLY_OVERTIME_LIMIT_MINUTES = 2700;
    private static final int YEARLY_OVERTIME_LIMIT_MINUTES = 21600;
    private static final BigDecimal OBLIGATION_DAYS = new BigDecimal("5.0");

    private final DailyAttendanceRepository dailyAttendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeDepartmentRepository employeeDepartmentRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    public AdminReportServiceImpl(
            DailyAttendanceRepository dailyAttendanceRepository,
            EmployeeRepository employeeRepository,
            EmployeeDepartmentRepository employeeDepartmentRepository,
            DepartmentRepository departmentRepository,
            LeaveBalanceRepository leaveBalanceRepository) {
        this.dailyAttendanceRepository = dailyAttendanceRepository;
        this.employeeRepository = employeeRepository;
        this.employeeDepartmentRepository = employeeDepartmentRepository;
        this.departmentRepository = departmentRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
    }

    @Override
    public OvertimeReportResponse getOvertimeReport(int year, int month) {
        var employees = employeeRepository.findAll().stream()
                .filter(Employee::isActive)
                .toList();

        YearMonth ym = YearMonth.of(year, month);
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();

        var monthlyAttendances = dailyAttendanceRepository.findAllByDateRange(monthStart, monthEnd);
        Map<Long, Integer> monthlyOvertimeByEmployee = monthlyAttendances.stream()
                .collect(Collectors.groupingBy(
                        DailyAttendance::getEmployeeId,
                        Collectors.summingInt(DailyAttendance::getOvertimeMinutes)));

        int fiscalYear = month >= 4 ? year : year - 1;
        LocalDate fiscalYearStart = LocalDate.of(fiscalYear, 4, 1);
        var yearlyAttendances = dailyAttendanceRepository.findAllByDateRange(fiscalYearStart, monthEnd);
        Map<Long, Integer> yearlyOvertimeByEmployee = yearlyAttendances.stream()
                .collect(Collectors.groupingBy(
                        DailyAttendance::getEmployeeId,
                        Collectors.summingInt(DailyAttendance::getOvertimeMinutes)));

        List<Long> employeeIds = employees.stream().map(Employee::getId).toList();
        Map<Long, String> deptNameByEmployeeId = buildDepartmentNameMap(employeeIds);

        List<OvertimeRecordResponse> records = employees.stream()
                .map(emp -> {
                    int monthlyOvertime = monthlyOvertimeByEmployee.getOrDefault(emp.getId(), 0);
                    int yearlyOvertime = yearlyOvertimeByEmployee.getOrDefault(emp.getId(), 0);
                    String deptName = deptNameByEmployeeId.getOrDefault(emp.getId(), "");
                    return new OvertimeRecordResponse(
                            emp.getId(),
                            emp.getName(),
                            deptName,
                            monthlyOvertime,
                            yearlyOvertime,
                            monthlyOvertime > MONTHLY_OVERTIME_LIMIT_MINUTES,
                            yearlyOvertime > YEARLY_OVERTIME_LIMIT_MINUTES);
                })
                .toList();

        return new OvertimeReportResponse(year, month, records);
    }

    @Override
    public LeaveObligationResponse getLeaveObligationReport(int fiscalYear) {
        var employees = employeeRepository.findAll().stream()
                .filter(Employee::isActive)
                .toList();

        var balances = leaveBalanceRepository.findByFiscalYear(fiscalYear);
        Map<Long, BigDecimal> usedDaysByEmployee = balances.stream()
                .collect(Collectors.groupingBy(
                        LeaveBalance::getEmployeeId,
                        Collectors.reducing(BigDecimal.ZERO, LeaveBalance::getUsedDays, BigDecimal::add)));

        List<Long> underObligationIds = employees.stream()
                .filter(emp -> {
                    BigDecimal used = usedDaysByEmployee.getOrDefault(emp.getId(), BigDecimal.ZERO);
                    return used.compareTo(OBLIGATION_DAYS) < 0;
                })
                .map(Employee::getId)
                .toList();

        Map<Long, String> deptNameByEmployeeId = buildDepartmentNameMap(underObligationIds);

        List<LeaveObligationRecordResponse> records = employees.stream()
                .filter(emp -> underObligationIds.contains(emp.getId()))
                .map(emp -> {
                    BigDecimal used = usedDaysByEmployee.getOrDefault(emp.getId(), BigDecimal.ZERO);
                    BigDecimal remaining = OBLIGATION_DAYS.subtract(used);
                    String deptName = deptNameByEmployeeId.getOrDefault(emp.getId(), "");
                    return new LeaveObligationRecordResponse(
                            emp.getId(), emp.getName(), deptName, used, remaining);
                })
                .toList();

        return new LeaveObligationResponse(fiscalYear, records);
    }

    private Map<Long, String> buildDepartmentNameMap(List<Long> employeeIds) {
        if (employeeIds.isEmpty()) {
            return Map.of();
        }
        var memberships = employeeDepartmentRepository.findByEmployeeIdInAndEndDateIsNull(employeeIds);
        Map<Long, EmployeeDepartment> primaryByEmployee = memberships.stream()
                .collect(Collectors.toMap(
                        EmployeeDepartment::getEmployeeId,
                        Function.identity(),
                        (a, b) -> a.isPrimary() ? a : b));

        List<Long> deptIds = primaryByEmployee.values().stream()
                .map(EmployeeDepartment::getDepartmentId)
                .distinct()
                .toList();

        Map<Long, String> deptNameById = departmentRepository.findAllById(deptIds).stream()
                .collect(Collectors.toMap(Department::getId, Department::getName));

        return primaryByEmployee.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> deptNameById.getOrDefault(e.getValue().getDepartmentId(), "")));
    }
}
