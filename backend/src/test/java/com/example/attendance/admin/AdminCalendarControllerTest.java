package com.example.attendance.admin;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.attendance.admin.dto.WorkCalendarCreateRequest;
import com.example.attendance.admin.dto.WorkCalendarResponse;
import com.example.attendance.admin.dto.WorkCalendarUpdateRequest;
import com.example.attendance.common.config.SecurityConfig;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCalendarController.class)
@Import(SecurityConfig.class)
@EnableMethodSecurity
@ActiveProfiles("test")
class AdminCalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WorkCalendarService workCalendarService;

    @Test
    @DisplayName("GET /api/v1/admin/calendars - 年度指定でカレンダー一覧取得")
    @WithMockUser(roles = "ADMIN")
    void getCalendars_returnsOk() throws Exception {
        var calendars = List.of(
                new WorkCalendarResponse(1L, LocalDate.of(2026, 1, 1), "HOLIDAY", "元日", 2025),
                new WorkCalendarResponse(2L, LocalDate.of(2026, 5, 3), "HOLIDAY", "憲法記念日", 2025));
        when(workCalendarService.findByFiscalYear(2025)).thenReturn(calendars);

        mockMvc.perform(get("/api/v1/admin/calendars")
                        .param("fiscalYear", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].calendarDate").value("2026-01-01"))
                .andExpect(jsonPath("$[0].description").value("元日"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/calendars - 休日登録")
    @WithMockUser(roles = "ADMIN")
    void createCalendar_returnsCreated() throws Exception {
        var request = new WorkCalendarCreateRequest(
                LocalDate.of(2026, 12, 29), "COMPANY_HOLIDAY", "年末休暇", 2026);
        var response = new WorkCalendarResponse(1L, request.calendarDate(), "COMPANY_HOLIDAY", "年末休暇", 2026);
        when(workCalendarService.create(any(WorkCalendarCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/calendars")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.dayType").value("COMPANY_HOLIDAY"));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/calendars/{id} - 休日更新")
    @WithMockUser(roles = "ADMIN")
    void updateCalendar_returnsOk() throws Exception {
        var request = new WorkCalendarUpdateRequest("COMPANY_HOLIDAY", "会社休日");
        var response = new WorkCalendarResponse(1L, LocalDate.of(2026, 1, 1), "COMPANY_HOLIDAY", "会社休日", 2025);
        when(workCalendarService.update(eq(1L), any(WorkCalendarUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/admin/calendars/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayType").value("COMPANY_HOLIDAY"))
                .andExpect(jsonPath("$.description").value("会社休日"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/calendars/{id} - 休日削除")
    @WithMockUser(roles = "ADMIN")
    void deleteCalendar_returnsNoContent() throws Exception {
        doNothing().when(workCalendarService).delete(1L);

        mockMvc.perform(delete("/api/v1/admin/calendars/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/calendars/{id} - 存在しないID")
    @WithMockUser(roles = "ADMIN")
    void deleteCalendar_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("WorkCalendar", 999L))
                .when(workCalendarService).delete(999L);

        mockMvc.perform(delete("/api/v1/admin/calendars/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("管理者以外は403")
    @WithMockUser(roles = "EMPLOYEE")
    void getCalendars_nonAdmin_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/calendars")
                        .param("fiscalYear", "2025"))
                .andExpect(status().isForbidden());
    }
}
