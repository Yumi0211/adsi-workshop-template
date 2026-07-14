package com.example.attendance.admin;

import com.example.attendance.admin.dto.LeaveObligationResponse;
import com.example.attendance.admin.dto.OvertimeReportResponse;

public interface AdminReportService {

    OvertimeReportResponse getOvertimeReport(int year, int month);

    LeaveObligationResponse getLeaveObligationReport(int fiscalYear);
}
