package com.example.attendance.leave;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.common.enums.Role;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.EmployeeRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    private Employee createEmployee() {
        return employeeRepository.save(Employee.builder()
                .employeeCode("EMP001").name("田中太郎").email("tanaka@example.com")
                .role(Role.EMPLOYEE).hireDate(LocalDate.of(2024, 4, 1)).active(true)
                .build());
    }

    @Test
    @DisplayName("GET /api/v1/leaves/balance — 200で残高が返る")
    @WithMockUser(roles = {"ADMIN"})
    void getBalance_returns200() throws Exception {
        var emp = createEmployee();
        leaveBalanceRepository.save(LeaveBalance.builder()
                .employeeId(emp.getId()).fiscalYear(2026)
                .grantedDays(new BigDecimal("10.0")).usedDays(new BigDecimal("2.0"))
                .carriedOverDays(BigDecimal.ZERO)
                .grantDate(LocalDate.of(2026, 4, 1)).expiryDate(LocalDate.of(2028, 3, 31))
                .build());

        mockMvc.perform(get("/api/v1/leaves/balance")
                        .header("X-Employee-Id", emp.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remainingDays").value(8.0))
                .andExpect(jsonPath("$.balances").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/leaves/requests — 201で申請が作成される")
    @WithMockUser(roles = {"ADMIN"})
    void createRequest_validBody_returns201() throws Exception {
        var emp = createEmployee();
        leaveBalanceRepository.save(LeaveBalance.builder()
                .employeeId(emp.getId()).fiscalYear(2026)
                .grantedDays(new BigDecimal("10.0")).usedDays(new BigDecimal("2.0"))
                .carriedOverDays(BigDecimal.ZERO)
                .grantDate(LocalDate.of(2026, 4, 1)).expiryDate(LocalDate.of(2028, 3, 31))
                .build());

        String json = """
                {
                    "leaveDate": "2026-07-20",
                    "leaveType": "FULL",
                    "reason": "旅行"
                }
                """;

        mockMvc.perform(post("/api/v1/leaves/requests")
                        .header("X-Employee-Id", emp.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.leaveType").value("FULL"));
    }

    @Test
    @DisplayName("POST /api/v1/leaves/requests — 残高不足で400")
    @WithMockUser(roles = {"ADMIN"})
    void createRequest_insufficientBalance_returns400() throws Exception {
        var emp = createEmployee();
        leaveBalanceRepository.save(LeaveBalance.builder()
                .employeeId(emp.getId()).fiscalYear(2026)
                .grantedDays(new BigDecimal("10.0")).usedDays(new BigDecimal("10.0"))
                .carriedOverDays(BigDecimal.ZERO)
                .grantDate(LocalDate.of(2026, 4, 1)).expiryDate(LocalDate.of(2028, 3, 31))
                .build());

        String json = """
                {
                    "leaveDate": "2026-07-20",
                    "leaveType": "FULL"
                }
                """;

        mockMvc.perform(post("/api/v1/leaves/requests")
                        .header("X-Employee-Id", emp.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/leaves/requests — 200で申請一覧が返る")
    @WithMockUser(roles = {"ADMIN"})
    void getRequests_returns200() throws Exception {
        var emp = createEmployee();

        mockMvc.perform(get("/api/v1/leaves/requests")
                        .header("X-Employee-Id", emp.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("未認証ユーザーは401が返る")
    void getBalance_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/leaves/balance")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
