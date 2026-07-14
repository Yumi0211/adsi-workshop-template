package com.example.attendance.attendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkCalendarRepository extends JpaRepository<WorkCalendar, Long> {

    Optional<WorkCalendar> findByCalendarDate(LocalDate calendarDate);

    List<WorkCalendar> findByFiscalYearOrderByCalendarDateAsc(int fiscalYear);

    boolean existsByCalendarDate(LocalDate calendarDate);
}
