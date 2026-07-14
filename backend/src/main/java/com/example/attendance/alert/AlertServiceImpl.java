package com.example.attendance.alert;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.alert.dto.AlertPageResponse;
import com.example.attendance.alert.dto.AlertResponse;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.EmployeeRepository;

@Service
@Transactional(readOnly = true)
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final EmployeeRepository employeeRepository;

    public AlertServiceImpl(AlertRepository alertRepository,
                            EmployeeRepository employeeRepository) {
        this.alertRepository = alertRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<AlertResponse> getMyAlerts(Long employeeId) {
        var alerts = alertRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
        var employeeNames = resolveEmployeeNames(alerts);
        return alerts.stream()
                .map(alert -> toResponse(alert, employeeNames))
                .toList();
    }

    @Override
    public AlertPageResponse getAllAlerts(int page, int size) {
        var pageable = PageRequest.of(page, size);
        var alertPage = alertRepository.findAllByOrderByCreatedAtDesc(pageable);
        var alerts = alertPage.getContent();
        var employeeNames = resolveEmployeeNames(alerts);
        var content = alerts.stream()
                .map(alert -> toResponse(alert, employeeNames))
                .toList();
        return new AlertPageResponse(content, page, size, alertPage.getTotalElements());
    }

    @Override
    @Transactional
    public AlertResponse acknowledge(Long alertId) {
        var alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        alert.setAcknowledged(true);
        var saved = alertRepository.save(alert);
        var employeeNames = resolveEmployeeNames(List.of(saved));
        return toResponse(saved, employeeNames);
    }

    private Map<Long, String> resolveEmployeeNames(List<Alert> alerts) {
        var employeeIds = alerts.stream()
                .map(Alert::getEmployeeId)
                .distinct()
                .toList();
        return employeeRepository.findAllById(employeeIds).stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName));
    }

    private AlertResponse toResponse(Alert alert, Map<Long, String> employeeNames) {
        var employeeName = employeeNames.getOrDefault(alert.getEmployeeId(), "不明");
        return new AlertResponse(
                alert.getId(),
                alert.getEmployeeId(),
                employeeName,
                alert.getType().name(),
                alert.getMessage(),
                alert.getCreatedAt(),
                alert.isAcknowledged()
        );
    }
}
