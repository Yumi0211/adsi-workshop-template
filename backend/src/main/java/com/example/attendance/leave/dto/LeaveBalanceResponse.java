package com.example.attendance.leave.dto;

import java.math.BigDecimal;
import java.util.List;

public record LeaveBalanceResponse(
        int fiscalYear,
        BigDecimal totalGrantedDays,
        BigDecimal usedDays,
        BigDecimal remainingDays,
        BigDecimal carriedOverDays,
        BigDecimal obligationDays,
        List<LeaveBalanceDetailResponse> balances
) {}
