package com.example.attendance.attendance;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.common.enums.AttendanceStatus;
import com.example.attendance.common.enums.Role;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.EmployeeRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DailyAttendanceRepository dailyAttendanceRepository;

    private static final ZoneOffset JST = ZoneOffset.ofHours(9);

    @BeforeEach
    void setUp() {
        var employee = Employee.builder()
                .employeeCode("EMP001")
                .name("田中太郎")
                .email("tanaka@example.com")
                .role(Role.EMPLOYEE)
                .hireDate(LocalDate.of(2020, 4, 1))
                .build();
        employeeRepository.save(employee);

        var attendance = DailyAttendance.builder()
                .employeeId(employee.getId())
                .attendanceDate(LocalDate.of(2026, 7, 1))
                .clockIn(OffsetDateTime.of(2026, 7, 1, 9, 0, 0, 0, JST))
                .clockOut(OffsetDateTime.of(2026, 7, 1, 17, 30, 0, 0, JST))
                .breakMinutes(60)
                .workingMinutes(450)
                .overtimeMinutes(0)
                .nightMinutes(0)
                .holidayWork(false)
                .status(AttendanceStatus.PRESENT)
                .build();
        dailyAttendanceRepository.save(attendance);
    }

    @Test
    @DisplayName("GET /api/v1/attendances/monthly: 200 と月次勤怠を返す")
    @WithMockUser(username = "tanaka@example.com")
    void getMonthlyAttendance_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/attendances/monthly")
                        .param("year", "2026")
                        .param("month", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.month").value(7))
                .andExpect(jsonPath("$.records").isArray())
                .andExpect(jsonPath("$.records[0].workingMinutes").value(450));
    }

    @Test
    @DisplayName("GET /api/v1/attendances/monthly/summary: 200 とサマリーを返す")
    @WithMockUser(username = "tanaka@example.com")
    void getMonthlySummary_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/attendances/monthly/summary")
                        .param("year", "2026")
                        .param("month", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalWorkingMinutes").value(450))
                .andExpect(jsonPath("$.workingDays").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/departments/{id}/attendances/monthly: APPROVER は 200 を返す")
    @WithMockUser(username = "manager@example.com", roles = {"APPROVER"})
    void getDepartmentMonthlyAttendance_approver_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/departments/1/attendances/monthly")
                        .param("year", "2026")
                        .param("month", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/departments/{id}/attendances/monthly: 一般社員は 403 を返す")
    @WithMockUser(username = "tanaka@example.com", roles = {"EMPLOYEE"})
    void getDepartmentMonthlyAttendance_employee_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/departments/1/attendances/monthly")
                        .param("year", "2026")
                        .param("month", "7"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("未認証アクセスは 401 を返す")
    void unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/attendances/monthly")
                        .param("year", "2026")
                        .param("month", "7"))
                .andExpect(status().isUnauthorized());
    }
}
