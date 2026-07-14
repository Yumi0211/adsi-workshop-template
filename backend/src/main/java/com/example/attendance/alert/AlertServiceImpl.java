package com.example.attendance.alert;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.alert.dto.AlertPageResponse;
import com.example.attendance.alert.dto.AlertResponse;
import com.example.attendance.common.exception.ResourceNotFoundException;
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
        return alerts.stream()
                .map(alert -> toResponse(alert))
                .toList();
    }

    @Override
    public AlertPageResponse getAllAlerts(int page, int size) {
        var pageable = PageRequest.of(page, size);
        var alertPage = alertRepository.findAllByOrderByCreatedAtDesc(pageable);
        var content = alertPage.getContent().stream()
                .map(alert -> toResponse(alert))
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
        return toResponse(saved);
    }

    private AlertResponse toResponse(Alert alert) {
        var employeeName = employeeRepository.findById(alert.getEmployeeId())
                .map(e -> e.getName())
                .orElse("不明");
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
