package com.example.attendance.attendance;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.example.attendance.common.enums.AttendanceStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "daily_attendances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "clock_in")
    private OffsetDateTime clockIn;

    @Column(name = "clock_out")
    private OffsetDateTime clockOut;

    @Column(name = "break_minutes", nullable = false)
    @Builder.Default
    private int breakMinutes = 0;

    @Column(name = "working_minutes", nullable = false)
    @Builder.Default
    private int workingMinutes = 0;

    @Column(name = "overtime_minutes", nullable = false)
    @Builder.Default
    private int overtimeMinutes = 0;

    @Column(name = "night_minutes", nullable = false)
    @Builder.Default
    private int nightMinutes = 0;

    @Column(name = "is_holiday_work", nullable = false)
    @Builder.Default
    private boolean holidayWork = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
