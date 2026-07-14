package com.example.attendance.alert;

import java.util.List;

import com.example.attendance.alert.dto.AlertPageResponse;
import com.example.attendance.alert.dto.AlertResponse;

public interface AlertService {

    List<AlertResponse> getMyAlerts(Long employeeId);

    AlertPageResponse getAllAlerts(int page, int size);

    AlertResponse acknowledge(Long alertId);
}
