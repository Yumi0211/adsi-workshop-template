package com.example.attendance.approval;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.attendance.approval.dto.ApprovalRequestDetailResponse;
import com.example.attendance.approval.dto.ApprovalRequestResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ApprovalControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ApprovalService approvalService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ApprovalController(approvalService)).build();
    }

    @Test
    @DisplayName("POST /api/v1/time-corrections: 201")
    void postTimeCorrection_returns201() throws Exception {
        var response = new ApprovalRequestResponse(
                1L, 10L, 20L, "TIME_CORRECTION", "PENDING",
                LocalDate.of(2026, 7, 14), "電車遅延", null, null
        );

        when(approvalService.createTimeCorrection(eq(10L), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/time-corrections")
                        .param("applicantId", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetDate": "2026-07-10",
                                  "correctedClockIn": "2026-07-10T08:30:00+09:00",
                                  "reason": "電車遅延"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/v1/approvals/pending: 200")
    void getPending_returns200WithList() throws Exception {
        var response = new ApprovalRequestResponse(
                1L, 10L, 20L, "TIME_CORRECTION", "PENDING",
                LocalDate.of(2026, 7, 14), null, null, null
        );

        when(approvalService.getPendingRequests(20L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/approvals/pending")
                        .param("approverId", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/v1/approvals/{id}: 200")
    void getDetail_returns200() throws Exception {
        var response = new ApprovalRequestDetailResponse(
                1L, 10L, 20L, "TIME_CORRECTION", "PENDING",
                LocalDate.of(2026, 7, 14), "{}", "理由", null, null
        );

        when(approvalService.getRequestDetail(10L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/approvals/1")
                        .param("requesterId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.detail").value("{}"));
    }

    @Test
    @DisplayName("PUT /api/v1/approvals/{id}/approve: 200")
    void putApprove_returns200() throws Exception {
        var response = new ApprovalRequestResponse(
                1L, 10L, 20L, "TIME_CORRECTION", "APPROVED",
                LocalDate.of(2026, 7, 14), null,
                OffsetDateTime.of(2026, 7, 14, 10, 0, 0, 0, ZoneOffset.ofHours(9)), null
        );

        when(approvalService.approve(20L, 1L)).thenReturn(response);

        mockMvc.perform(put("/api/v1/approvals/1/approve")
                        .param("approverId", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("PUT /api/v1/approvals/{id}/reject: 200")
    void putReject_returns200() throws Exception {
        var response = new ApprovalRequestResponse(
                1L, 10L, 20L, "TIME_CORRECTION", "REJECTED",
                LocalDate.of(2026, 7, 14), null, null, "不明確"
        );

        when(approvalService.reject(eq(20L), eq(1L), eq("不明確"))).thenReturn(response);

        mockMvc.perform(put("/api/v1/approvals/1/reject")
                        .param("approverId", "20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rejectionReason": "不明確"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").value("不明確"));
    }

    @Test
    @DisplayName("GET /api/v1/my-requests: 200")
    void getMyRequests_returns200() throws Exception {
        var response = new ApprovalRequestResponse(
                1L, 10L, 20L, "LEAVE", "APPROVED",
                LocalDate.of(2026, 7, 14), null,
                OffsetDateTime.of(2026, 7, 14, 10, 0, 0, 0, ZoneOffset.ofHours(9)), null
        );

        when(approvalService.getMyRequests(10L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/my-requests")
                        .param("applicantId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].type").value("LEAVE"));
    }
}
