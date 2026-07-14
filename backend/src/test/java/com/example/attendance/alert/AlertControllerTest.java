package com.example.attendance.alert;

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

import com.example.attendance.common.enums.AlertType;
import com.example.attendance.common.enums.Role;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.EmployeeRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AlertRepository alertRepository;

    private static final ZoneOffset JST = ZoneOffset.ofHours(9);

    private Long employeeId;
    private Long alertId;

    @BeforeEach
    void setUp() {
        var employee = Employee.builder()
                .employeeCode("EMP001")
                .name("田中太郎")
                .email("tanaka@example.com")
                .role(Role.EMPLOYEE)
                .hireDate(LocalDate.of(2020, 4, 1))
                .build();
        employee = employeeRepository.save(employee);
        employeeId = employee.getId();

        var alert = Alert.builder()
                .employeeId(employeeId)
                .type(AlertType.OVERTIME_MONTHLY)
                .message("月間残業時間が45時間を超過しました（50時間30分）")
                .createdAt(OffsetDateTime.now(JST))
                .acknowledged(false)
                .build();
        alert = alertRepository.save(alert);
        alertId = alert.getId();
    }

    @Test
    @DisplayName("GET /api/v1/alerts/my: 自分のアラートを取得できる")
    @WithMockUser(username = "tanaka@example.com")
    void getMyAlerts_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/alerts/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("OVERTIME_MONTHLY"))
                .andExpect(jsonPath("$[0].employeeName").value("田中太郎"))
                .andExpect(jsonPath("$[0].acknowledged").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/admin/alerts: 管理者はアラート一覧を取得できる")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void getAdminAlerts_admin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/admin/alerts")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("OVERTIME_MONTHLY"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/admin/alerts: 一般社員は403")
    @WithMockUser(username = "tanaka@example.com", roles = {"EMPLOYEE"})
    void getAdminAlerts_employee_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/alerts")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/v1/admin/alerts/{id}/acknowledge: 管理者はアラートを確認済みにできる")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void acknowledgeAlert_admin_returns200() throws Exception {
        mockMvc.perform(put("/api/v1/admin/alerts/" + alertId + "/acknowledge"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acknowledged").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/alerts/{id}/acknowledge: 存在しないID → 404")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void acknowledgeAlert_notFound_returns404() throws Exception {
        mockMvc.perform(put("/api/v1/admin/alerts/9999/acknowledge"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("未認証アクセスは401")
    void unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/alerts/my"))
                .andExpect(status().isUnauthorized());
    }
}
