package com.example.attendance.admin;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.admin.dto.WorkCalendarCreateRequest;
import com.example.attendance.admin.dto.WorkCalendarResponse;
import com.example.attendance.admin.dto.WorkCalendarUpdateRequest;
import com.example.attendance.attendance.WorkCalendar;
import com.example.attendance.attendance.WorkCalendarRepository;
import com.example.attendance.common.enums.DayType;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.ResourceNotFoundException;

@Service
@Transactional
public class WorkCalendarServiceImpl implements WorkCalendarService {

    private final WorkCalendarRepository workCalendarRepository;

    public WorkCalendarServiceImpl(WorkCalendarRepository workCalendarRepository) {
        this.workCalendarRepository = workCalendarRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkCalendarResponse> findByFiscalYear(int fiscalYear) {
        return workCalendarRepository.findByFiscalYearOrderByCalendarDateAsc(fiscalYear).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public WorkCalendarResponse create(WorkCalendarCreateRequest request) {
        if (workCalendarRepository.existsByCalendarDate(request.calendarDate())) {
            throw new BusinessException("指定された日付は既に登録されています");
        }

        var calendar = WorkCalendar.builder()
                .calendarDate(request.calendarDate())
                .dayType(DayType.valueOf(request.dayType()))
                .description(request.description())
                .fiscalYear(request.fiscalYear())
                .build();

        calendar = workCalendarRepository.save(calendar);
        return toResponse(calendar);
    }

    @Override
    public WorkCalendarResponse update(Long id, WorkCalendarUpdateRequest request) {
        var calendar = workCalendarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkCalendar", id));

        calendar.setDayType(DayType.valueOf(request.dayType()));
        calendar.setDescription(request.description());

        calendar = workCalendarRepository.save(calendar);
        return toResponse(calendar);
    }

    @Override
    public void delete(Long id) {
        if (!workCalendarRepository.existsById(id)) {
            throw new ResourceNotFoundException("WorkCalendar", id);
        }
        workCalendarRepository.deleteById(id);
    }

    private WorkCalendarResponse toResponse(WorkCalendar calendar) {
        return new WorkCalendarResponse(
                calendar.getId(),
                calendar.getCalendarDate(),
                calendar.getDayType().name(),
                calendar.getDescription(),
                calendar.getFiscalYear());
    }
}
