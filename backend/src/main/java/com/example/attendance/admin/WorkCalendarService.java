package com.example.attendance.admin;

import java.util.List;

import com.example.attendance.admin.dto.WorkCalendarCreateRequest;
import com.example.attendance.admin.dto.WorkCalendarResponse;
import com.example.attendance.admin.dto.WorkCalendarUpdateRequest;

public interface WorkCalendarService {

    List<WorkCalendarResponse> findByFiscalYear(int fiscalYear);

    WorkCalendarResponse create(WorkCalendarCreateRequest request);

    WorkCalendarResponse update(Long id, WorkCalendarUpdateRequest request);

    void delete(Long id);
}
