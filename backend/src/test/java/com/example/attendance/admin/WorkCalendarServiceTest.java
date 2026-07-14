package com.example.attendance.admin;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.attendance.admin.dto.WorkCalendarCreateRequest;
import com.example.attendance.admin.dto.WorkCalendarUpdateRequest;
import com.example.attendance.attendance.WorkCalendar;
import com.example.attendance.attendance.WorkCalendarRepository;
import com.example.attendance.common.enums.DayType;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkCalendarServiceTest {

    @Mock
    private WorkCalendarRepository workCalendarRepository;

    private WorkCalendarServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WorkCalendarServiceImpl(workCalendarRepository);
    }

    @Test
    @DisplayName("年度を指定してカレンダー一覧を取得できる")
    void findByFiscalYear_returnsCalendars() {
        var calendars = List.of(
                buildCalendar(1L, LocalDate.of(2026, 1, 1), DayType.HOLIDAY, "元日"),
                buildCalendar(2L, LocalDate.of(2026, 5, 3), DayType.HOLIDAY, "憲法記念日"));
        when(workCalendarRepository.findByFiscalYearOrderByCalendarDateAsc(2025))
                .thenReturn(calendars);

        var result = service.findByFiscalYear(2025);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).calendarDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(result.get(0).description()).isEqualTo("元日");
    }

    @Test
    @DisplayName("休日を新規登録できる")
    void create_validRequest_returnsCreated() {
        var request = new WorkCalendarCreateRequest(
                LocalDate.of(2026, 12, 29), "COMPANY_HOLIDAY", "年末休暇", 2026);
        when(workCalendarRepository.existsByCalendarDate(request.calendarDate()))
                .thenReturn(false);
        when(workCalendarRepository.save(any(WorkCalendar.class)))
                .thenAnswer(inv -> {
                    WorkCalendar saved = inv.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        var result = service.create(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.calendarDate()).isEqualTo(LocalDate.of(2026, 12, 29));
        assertThat(result.dayType()).isEqualTo("COMPANY_HOLIDAY");
        assertThat(result.description()).isEqualTo("年末休暇");
    }

    @Test
    @DisplayName("既に登録済みの日付を登録しようとするとエラー")
    void create_duplicateDate_throwsBusinessException() {
        var request = new WorkCalendarCreateRequest(
                LocalDate.of(2026, 1, 1), "HOLIDAY", "元日", 2025);
        when(workCalendarRepository.existsByCalendarDate(request.calendarDate()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("既に登録されています");
    }

    @Test
    @DisplayName("カレンダーを更新できる")
    void update_existingId_returnsUpdated() {
        var existing = buildCalendar(1L, LocalDate.of(2026, 1, 1), DayType.HOLIDAY, "元日");
        when(workCalendarRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(workCalendarRepository.save(any(WorkCalendar.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var request = new WorkCalendarUpdateRequest("COMPANY_HOLIDAY", "会社休日");
        var result = service.update(1L, request);

        assertThat(result.dayType()).isEqualTo("COMPANY_HOLIDAY");
        assertThat(result.description()).isEqualTo("会社休日");
    }

    @Test
    @DisplayName("存在しないIDの更新はエラー")
    void update_nonExistingId_throwsNotFound() {
        when(workCalendarRepository.findById(999L)).thenReturn(Optional.empty());

        var request = new WorkCalendarUpdateRequest("HOLIDAY", "test");

        assertThatThrownBy(() -> service.update(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("カレンダーを削除できる")
    void delete_existingId_deletes() {
        when(workCalendarRepository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(workCalendarRepository).deleteById(1L);
    }

    @Test
    @DisplayName("存在しないIDの削除はエラー")
    void delete_nonExistingId_throwsNotFound() {
        when(workCalendarRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private WorkCalendar buildCalendar(Long id, LocalDate date, DayType dayType, String desc) {
        return WorkCalendar.builder()
                .id(id)
                .calendarDate(date)
                .dayType(dayType)
                .description(desc)
                .fiscalYear(date.getMonthValue() >= 4 ? date.getYear() : date.getYear() - 1)
                .createdAt(OffsetDateTime.now())
                .build();
    }
}
