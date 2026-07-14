package com.example.attendance.attendance.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkDurationTest {

    @Test
    @DisplayName("0分の場合 toHours は 0h00m を返す")
    void toHours_zeroMinutes_returns0h00m() {
        var duration = WorkDuration.of(0);
        assertThat(duration.toHours()).isEqualTo("0h00m");
    }

    @Test
    @DisplayName("450分は 7h30m を返す")
    void toHours_450minutes_returns7h30m() {
        var duration = WorkDuration.of(450);
        assertThat(duration.toHours()).isEqualTo("7h30m");
    }

    @Test
    @DisplayName("60分は 1h00m を返す")
    void toHours_60minutes_returns1h00m() {
        var duration = WorkDuration.of(60);
        assertThat(duration.toHours()).isEqualTo("1h00m");
    }

    @Test
    @DisplayName("負の分数は IllegalArgumentException をスローする")
    void of_negativeMinutes_throwsException() {
        assertThatThrownBy(() -> WorkDuration.of(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("基準450分で480分は残業30分")
    void overtimeMinutes_480against450_returns30() {
        var duration = WorkDuration.of(480);
        assertThat(duration.overtimeMinutes(450)).isEqualTo(30);
    }

    @Test
    @DisplayName("基準450分で400分は残業0分")
    void overtimeMinutes_400against450_returns0() {
        var duration = WorkDuration.of(400);
        assertThat(duration.overtimeMinutes(450)).isEqualTo(0);
    }

    @Test
    @DisplayName("minutes で分数を取得できる")
    void minutes_returnsValue() {
        var duration = WorkDuration.of(123);
        assertThat(duration.minutes()).isEqualTo(123);
    }
}
