package com.example.attendance.leave;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    List<LeaveBalance> findByEmployeeIdAndFiscalYear(Long employeeId, int fiscalYear);

    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.employeeId = :employeeId AND lb.expiryDate > :asOf")
    List<LeaveBalance> findActiveByEmployeeId(@Param("employeeId") Long employeeId, @Param("asOf") LocalDate asOf);
}
