package com.example.attendance.attendance;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.example.attendance.common.enums.DayType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "work_calendars")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "calendar_date", nullable = false, unique = true)
    private LocalDate calendarDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false, length = 20)
    private DayType dayType;

    @Column(length = 100)
    private String description;

    @Column(name = "fiscal_year", nullable = false)
    private int fiscalYear;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
