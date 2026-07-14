package com.example.attendance.leave;

import java.util.List;

import com.example.attendance.leave.dto.LeaveBalanceResponse;
import com.example.attendance.leave.dto.LeaveRequestCreateRequest;
import com.example.attendance.leave.dto.LeaveRequestResponse;

public interface LeaveService {

    LeaveBalanceResponse getBalance(Long employeeId);

    LeaveRequestResponse createRequest(Long employeeId, LeaveRequestCreateRequest request);

    List<LeaveRequestResponse> getRequests(Long employeeId);
}
