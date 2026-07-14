package com.example.attendance.attendance;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DailyAttendanceRepository extends JpaRepository<DailyAttendance, Long> {

    @Query("SELECT da FROM DailyAttendance da WHERE da.employeeId = :employeeId " +
            "AND da.attendanceDate >= :startDate AND da.attendanceDate <= :endDate " +
            "ORDER BY da.attendanceDate ASC")
    List<DailyAttendance> findByEmployeeIdAndMonth(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT da FROM DailyAttendance da WHERE da.employeeId IN :employeeIds " +
            "AND da.attendanceDate >= :startDate AND da.attendanceDate <= :endDate " +
            "ORDER BY da.employeeId ASC, da.attendanceDate ASC")
    List<DailyAttendance> findByEmployeeIdsAndMonth(
            @Param("employeeIds") List<Long> employeeIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT da FROM DailyAttendance da " +
            "WHERE da.attendanceDate >= :startDate AND da.attendanceDate <= :endDate " +
            "ORDER BY da.employeeId ASC, da.attendanceDate ASC")
    List<DailyAttendance> findAllByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
