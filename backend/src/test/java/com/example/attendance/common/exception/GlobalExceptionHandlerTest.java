package com.example.attendance.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes = {
        GlobalExceptionHandlerTest.TestErrorController.class,
        GlobalExceptionHandler.class
})
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class TestErrorController {
        @GetMapping("/test/not-found")
        public void throwNotFound() {
            throw new ResourceNotFoundException("Employee", 999L);
        }

        @GetMapping("/test/business-error")
        public void throwBusiness() {
            throw new BusinessException("打刻順序が不正です");
        }
    }

    @Test
    @DisplayName("ResourceNotFoundException → 404 RFC7807形式")
    @WithMockUser
    void notFound_returns404WithProblemDetail() throws Exception {
        mockMvc.perform(get("/test/not-found").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Employee not found with id: 999"));
    }

    @Test
    @DisplayName("BusinessException → 400 RFC7807形式")
    @WithMockUser
    void businessError_returns400WithProblemDetail() throws Exception {
        mockMvc.perform(get("/test/business-error").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Business Error"))
                .andExpect(jsonPath("$.detail").value("打刻順序が不正です"));
    }
}
