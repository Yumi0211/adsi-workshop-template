package com.example.attendance.attendance.vo;

public record WorkDuration(int minutes) {

    public static WorkDuration of(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("minutes must be non-negative: " + minutes);
        }
        return new WorkDuration(minutes);
    }

    public String toHours() {
        return "%dh%02dm".formatted(minutes / 60, minutes % 60);
    }

    public int overtimeMinutes(int standardMinutes) {
        int overtime = minutes - standardMinutes;
        return Math.max(overtime, 0);
    }
}
