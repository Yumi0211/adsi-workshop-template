package com.example.attendance.leave.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.attendance.leave.LeaveBalance;

public record LeaveBalanceDetailResponse(
        LocalDate grantDate,
        LocalDate expiryDate,
        BigDecimal grantedDays,
        BigDecimal usedDays,
        BigDecimal remainingDays
) {
    public static LeaveBalanceDetailResponse from(LeaveBalance balance) {
        return new LeaveBalanceDetailResponse(
                balance.getGrantDate(),
                balance.getExpiryDate(),
                balance.getGrantedDays(),
                balance.getUsedDays(),
                balance.getRemainingDays()
        );
    }
}
