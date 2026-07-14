package com.example.attendance.attendance.vo;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeRangeTest {

    @Test
    @DisplayName("重複なしの場合 overlapMinutes は 0 を返す")
    void overlapMinutes_noOverlap_returns0() {
        var range1 = TimeRange.of(
                LocalDateTime.of(2026, 7, 14, 9, 0),
                LocalDateTime.of(2026, 7, 14, 17, 0));
        var range2 = TimeRange.of(
                LocalDateTime.of(2026, 7, 14, 22, 0),
                LocalDateTime.of(2026, 7, 15, 5, 0));

        assertThat(range1.overlapMinutes(range2)).isEqualTo(0);
    }

    @Test
    @DisplayName("完全重複の場合は短い方の長さを返す")
    void overlapMinutes_fullOverlap_returnsShorterDuration() {
        var range1 = TimeRange.of(
                LocalDateTime.of(2026, 7, 14, 22, 0),
                LocalDateTime.of(2026, 7, 15, 5, 0));
        var range2 = TimeRange.of(
                LocalDateTime.of(2026, 7, 14, 20, 0),
                LocalDateTime.of(2026, 7, 15, 8, 0));

        assertThat(range1.overlapMinutes(range2)).isEqualTo(420);
    }

    @Test
    @DisplayName("部分重複の場合は重複分のみ返す")
    void overlapMinutes_partialOverlap_returnsOverlapOnly() {
        var workRange = TimeRange.of(
                LocalDateTime.of(2026, 7, 14, 9, 0),
                LocalDateTime.of(2026, 7, 14, 23, 30));
        var nightRange = TimeRange.of(
                LocalDateTime.of(2026, 7, 14, 22, 0),
                LocalDateTime.of(2026, 7, 15, 5, 0));

        assertThat(workRange.overlapMinutes(nightRange)).isEqualTo(90);
    }

    @Test
    @DisplayName("durationMinutes は範囲の長さを分で返す")
    void durationMinutes_returnsCorrectDuration() {
        var range = TimeRange.of(
                LocalDateTime.of(2026, 7, 14, 9, 0),
                LocalDateTime.of(2026, 7, 14, 17, 30));

        assertThat(range.durationMinutes()).isEqualTo(510);
    }

    @Test
    @DisplayName("同一時刻の場合 durationMinutes は 0")
    void durationMinutes_sameTime_returns0() {
        var time = LocalDateTime.of(2026, 7, 14, 9, 0);
        var range = TimeRange.of(time, time);

        assertThat(range.durationMinutes()).isEqualTo(0);
    }

    @Test
    @DisplayName("start > end の場合 IllegalArgumentException をスローする")
    void of_startAfterEnd_throwsException() {
        var start = LocalDateTime.of(2026, 7, 14, 18, 0);
        var end = LocalDateTime.of(2026, 7, 14, 9, 0);

        assertThatThrownBy(() -> TimeRange.of(start, end))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
