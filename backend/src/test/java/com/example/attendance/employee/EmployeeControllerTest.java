package com.example.attendance.employee;

import java.time.LocalDate;
import java.util.List;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    @DisplayName("管理者が社員一覧を取得できる")
    @WithMockUser(roles = {"ADMIN"})
    void list_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/admin/employees").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("未認証ユーザーは401が返る")
    void list_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/employees").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("正常なリクエストで社員が登録される")
    @WithMockUser(roles = {"ADMIN"})
    void create_validBody_returns201() throws Exception {
        var dept = departmentRepository.save(Department.builder()
                .name("営業課")
                .level(com.example.attendance.common.enums.DepartmentLevel.SECTION)
                .active(true)
                .build());

        String json = """
                {
                    "employeeCode": "EMP001",
                    "name": "田中太郎",
                    "email": "tanaka@example.com",
                    "role": "EMPLOYEE",
                    "hireDate": "2024-04-01",
                    "departments": [{"departmentId": %d, "isPrimary": true}]
                }
                """.formatted(dept.getId());

        mockMvc.perform(post("/api/v1/admin/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeCode").value("EMP001"))
                .andExpect(jsonPath("$.name").value("田中太郎"));
    }

    @Test
    @DisplayName("バリデーションエラーで400が返る")
    @WithMockUser(roles = {"ADMIN"})
    void create_invalidBody_returns400() throws Exception {
        String json = """
                {
                    "employeeCode": "",
                    "name": "",
                    "email": "invalid-email",
                    "role": "EMPLOYEE",
                    "hireDate": "2024-04-01",
                    "departments": []
                }
                """;

        mockMvc.perform(post("/api/v1/admin/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
