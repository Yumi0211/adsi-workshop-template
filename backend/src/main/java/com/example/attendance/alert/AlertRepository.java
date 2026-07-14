package com.example.attendance.alert;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.attendance.common.enums.AlertType;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    Page<Alert> findAllByOrderByCreatedAtDesc(Pageable pageable);

    boolean existsByEmployeeIdAndTypeAndCreatedAtAfter(
            Long employeeId, AlertType type, OffsetDateTime after);
}
