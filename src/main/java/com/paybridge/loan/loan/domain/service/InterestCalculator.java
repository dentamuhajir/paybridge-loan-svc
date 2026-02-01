package com.paybridge.loan.loan.domain.service;


import com.paybridge.loan.loan.domain.enums.InterestType;
import com.paybridge.loan.loan.domain.exception.InvalidLoanException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class InterestCalculator {

    /**
     * Calculate TOTAL interest for a loan
     */
    public BigDecimal calculate(
            BigDecimal principal,
            InterestType interestType,
            BigDecimal annualRate,
            int tenorMonths
    ) {
        validate(principal, annualRate, tenorMonths);

        return switch (interestType) {
            case FLAT -> calculateFlat(principal, annualRate, tenorMonths);
            case ANNUITY -> calculateAnnuity(principal, annualRate, tenorMonths);
        };
    }

    /**
     * FLAT interest:
     * totalInterest = P × rate × tenor / (12 × 100)
     */
    private BigDecimal calculateFlat(
            BigDecimal principal,
            BigDecimal annualRate,
            int tenorMonths
    ) {
        return principal
                .multiply(annualRate)
                .multiply(BigDecimal.valueOf(tenorMonths))
                .divide(BigDecimal.valueOf(12 * 100), 2, RoundingMode.HALF_UP);
    }

    /**
     * ANNUITY interest:
     * - Calculate monthly installment (A)
     * - Total interest = (A × tenor) − principal
     */
    private BigDecimal calculateAnnuity(
            BigDecimal principal,
            BigDecimal annualRate,
            int tenorMonths
    ) {
        // r = annualRate / 12 / 100
        BigDecimal monthlyRate =
                annualRate.divide(BigDecimal.valueOf(12 * 100), 10, RoundingMode.HALF_UP);

        // (1 + r)^n
        BigDecimal onePlusRPowerN =
                BigDecimal.ONE.add(monthlyRate).pow(tenorMonths);

        // A = P × r × (1+r)^n / ((1+r)^n − 1)
        BigDecimal monthlyInstallment =
                principal
                        .multiply(monthlyRate)
                        .multiply(onePlusRPowerN)
                        .divide(
                                onePlusRPowerN.subtract(BigDecimal.ONE),
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal totalPayment =
                monthlyInstallment.multiply(BigDecimal.valueOf(tenorMonths));

        return totalPayment.subtract(principal);
    }

    private void validate(
            BigDecimal principal,
            BigDecimal annualRate,
            int tenorMonths
    ) {
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidLoanException("Principal must be greater than zero");
        }

        if (annualRate == null || annualRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidLoanException("Interest rate must be greater than zero");
        }

        if (tenorMonths <= 0) {
            throw new InvalidLoanException("Tenor months must be greater than zero");
        }
    }
}
