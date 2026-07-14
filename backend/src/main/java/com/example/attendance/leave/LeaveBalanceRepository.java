package com.example.attendance.leave;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    List<LeaveBalance> findByEmployeeIdAndFiscalYear(Long employeeId, int fiscalYear);
}
