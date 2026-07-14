package com.example.attendance.employee;

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

import com.example.attendance.common.enums.DepartmentLevel;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeDepartmentRepository employeeDepartmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("管理者が部門ツリーを取得できる")
    @WithMockUser(roles = {"ADMIN"})
    void tree_asAdmin_returns200() throws Exception {
        departmentRepository.save(Department.builder()
                .name("営業本部").level(DepartmentLevel.HEADQUARTERS).active(true).build());

        mockMvc.perform(get("/api/v1/admin/departments").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("営業本部"));
    }

    @Test
    @DisplayName("正常なリクエストで部門が登録される")
    @WithMockUser(roles = {"ADMIN"})
    void create_validBody_returns201() throws Exception {
        String json = """
                {
                    "name": "営業本部",
                    "level": "HEADQUARTERS",
                    "parentId": null
                }
                """;

        mockMvc.perform(post("/api/v1/admin/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("営業本部"))
                .andExpect(jsonPath("$.level").value("HEADQUARTERS"));
    }

    @Test
    @DisplayName("配下社員がいる部門の無効化は400エラー")
    @WithMockUser(roles = {"ADMIN"})
    void deactivate_hasEmployees_returns400() throws Exception {
        var emp = employeeRepository.save(Employee.builder()
                .employeeCode("EMP999").name("テスト社員").email("test@example.com")
                .role(com.example.attendance.common.enums.Role.EMPLOYEE)
                .hireDate(java.time.LocalDate.of(2024, 4, 1)).active(true).build());

        var dept = departmentRepository.save(Department.builder()
                .name("営業課").level(DepartmentLevel.SECTION).active(true).build());

        employeeDepartmentRepository.save(EmployeeDepartment.builder()
                .employeeId(emp.getId())
                .departmentId(dept.getId())
                .primary(true)
                .startDate(java.time.LocalDate.now())
                .build());

        mockMvc.perform(put("/api/v1/admin/departments/{id}/deactivate", dept.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("配下に所属社員がいるため無効化できません"));
    }
}
