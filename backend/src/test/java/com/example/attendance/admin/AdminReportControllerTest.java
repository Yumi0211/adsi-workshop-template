package com.example.attendance.admin;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.attendance.admin.dto.LeaveObligationRecordResponse;
import com.example.attendance.admin.dto.LeaveObligationResponse;
import com.example.attendance.admin.dto.OvertimeRecordResponse;
import com.example.attendance.admin.dto.OvertimeReportResponse;
import com.example.attendance.common.config.SecurityConfig;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminReportController.class)
@Import(SecurityConfig.class)
@EnableMethodSecurity
@ActiveProfiles("test")
class AdminReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminReportService adminReportService;

    @Test
    @DisplayName("GET /api/v1/admin/reports/overtime - 残業レポート取得")
    @WithMockUser(roles = "ADMIN")
    void getOvertimeReport_returnsOk() throws Exception {
        var records = List.of(
                new OvertimeRecordResponse(1L, "田中太郎", "開発部", 2700, 15000, false, false));
        var response = new OvertimeReportResponse(2026, 7, records);
        when(adminReportService.getOvertimeReport(2026, 7)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/reports/overtime")
                        .param("year", "2026")
                        .param("month", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.month").value(7))
                .andExpect(jsonPath("$.records[0].employeeName").value("田中太郎"))
                .andExpect(jsonPath("$.records[0].overtimeMinutes").value(2700));
    }

    @Test
    @DisplayName("GET /api/v1/admin/reports/leave-obligation - 有給未取得一覧取得")
    @WithMockUser(roles = "ADMIN")
    void getLeaveObligationReport_returnsOk() throws Exception {
        var records = List.of(
                new LeaveObligationRecordResponse(1L, "田中太郎", "開発部",
                        new BigDecimal("3.0"), new BigDecimal("2.0")));
        var response = new LeaveObligationResponse(2025, records);
        when(adminReportService.getLeaveObligationReport(2025)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/reports/leave-obligation")
                        .param("fiscalYear", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fiscalYear").value(2025))
                .andExpect(jsonPath("$.records[0].employeeName").value("田中太郎"))
                .andExpect(jsonPath("$.records[0].obligationRemaining").value(2.0));
    }

    @Test
    @DisplayName("管理者以外は403")
    @WithMockUser(roles = "EMPLOYEE")
    void getOvertimeReport_nonAdmin_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/reports/overtime")
                        .param("year", "2026")
                        .param("month", "7"))
                .andExpect(status().isForbidden());
    }
}
