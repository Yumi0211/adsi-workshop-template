package com.example.attendance.timerecord;

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

import com.example.attendance.common.enums.RecordSource;
import com.example.attendance.common.enums.Role;
import com.example.attendance.common.enums.TimeRecordType;
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
class TimeRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TimeRecordRepository timeRecordRepository;

    private Employee createTestEmployee() {
        return employeeRepository.save(Employee.builder()
                .employeeCode("EMP001")
                .name("田中太郎")
                .email("tanaka@example.com")
                .role(Role.EMPLOYEE)
                .hireDate(LocalDate.of(2024, 4, 1))
                .active(true)
                .build());
    }

    @Test
    @DisplayName("正常な出勤打刻で201が返る")
    @WithMockUser(username = "tanaka@example.com")
    void create_validClockIn_returns201() throws Exception {
        createTestEmployee();

        String json = """
                {
                    "type": "CLOCK_IN",
                    "source": "WEB"
                }
                """;

        mockMvc.perform(post("/api/v1/time-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("CLOCK_IN"))
                .andExpect(jsonPath("$.source").value("WEB"))
                .andExpect(jsonPath("$.recordDate").exists())
                .andExpect(jsonPath("$.recordedAt").exists());
    }

    @Test
    @DisplayName("二重出勤打刻で400が返る")
    @WithMockUser(username = "tanaka@example.com")
    void create_duplicateClockIn_returns400() throws Exception {
        var emp = createTestEmployee();

        timeRecordRepository.save(TimeRecord.builder()
                .employeeId(emp.getId())
                .recordDate(LocalDate.now())
                .type(TimeRecordType.CLOCK_IN)
                .recordedAt(java.time.OffsetDateTime.now())
                .source(RecordSource.WEB)
                .build());

        String json = """
                {
                    "type": "CLOCK_IN",
                    "source": "WEB"
                }
                """;

        mockMvc.perform(post("/api/v1/time-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("不正なリクエストボディで400が返る")
    @WithMockUser(username = "tanaka@example.com")
    void create_invalidBody_returns400() throws Exception {
        createTestEmployee();
        String json = """
                {
                    "type": null,
                    "source": null
                }
                """;

        mockMvc.perform(post("/api/v1/time-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("指定日の打刻一覧取得で200が返る")
    @WithMockUser(username = "tanaka@example.com")
    void findByDate_returnsRecords() throws Exception {
        var emp = createTestEmployee();

        timeRecordRepository.save(TimeRecord.builder()
                .employeeId(emp.getId())
                .recordDate(LocalDate.of(2026, 7, 14))
                .type(TimeRecordType.CLOCK_IN)
                .recordedAt(java.time.OffsetDateTime.now())
                .source(RecordSource.WEB)
                .build());

        mockMvc.perform(get("/api/v1/time-records")
                        .param("date", "2026-07-14")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("CLOCK_IN"));
    }

    @Test
    @DisplayName("未認証ユーザーは401が返る")
    void create_unauthenticated_returns401() throws Exception {
        String json = """
                {
                    "type": "CLOCK_IN",
                    "source": "WEB"
                }
                """;

        mockMvc.perform(post("/api/v1/time-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }
}
