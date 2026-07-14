package com.example.attendance.approval;

import java.time.OffsetDateTime;
import java.util.function.Supplier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApprovalConfig {

    @Bean
    public Supplier<OffsetDateTime> nowSupplier() {
        return OffsetDateTime::now;
    }
}
