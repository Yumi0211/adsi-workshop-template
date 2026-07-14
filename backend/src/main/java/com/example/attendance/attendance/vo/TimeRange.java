package com.example.attendance.attendance.vo;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record TimeRange(LocalDateTime start, LocalDateTime end) {

    public static TimeRange of(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("start must not be after end: " + start + " > " + end);
        }
        return new TimeRange(start, end);
    }

    public int overlapMinutes(TimeRange other) {
        LocalDateTime overlapStart = this.start.isAfter(other.start) ? this.start : other.start;
        LocalDateTime overlapEnd = this.end.isBefore(other.end) ? this.end : other.end;

        if (overlapStart.isAfter(overlapEnd) || overlapStart.isEqual(overlapEnd)) {
            return 0;
        }
        return (int) ChronoUnit.MINUTES.between(overlapStart, overlapEnd);
    }

    public int durationMinutes() {
        return (int) ChronoUnit.MINUTES.between(start, end);
    }
}
