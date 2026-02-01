package com.paybridge.loan.loan.domain.model;

import com.paybridge.loan.loan.domain.enums.InterestType;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductTenor(
        UUID tenorId,
        int tenorMonths,
        BigDecimal interestRate,
        BigDecimal adminFee,
        InterestType interestType
) {}
