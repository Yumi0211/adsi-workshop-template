package com.example.attendance.leave;

import java.time.LocalDate;
import java.util.function.Supplier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LeaveConfig {

    @Bean
    public Supplier<LocalDate> todaySupplier() {
        return LocalDate::now;
    }
}
