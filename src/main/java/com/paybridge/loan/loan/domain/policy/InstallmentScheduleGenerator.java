package com.paybridge.loan.loan.domain.policy;

import com.paybridge.loan.loan.domain.model.Loan;
import com.paybridge.loan.loan.domain.model.LoanInstallment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class InstallmentScheduleGenerator {

    public List<LoanInstallment> generate(Loan loan) {

        int tenor = loan.getTenorMonths();

        BigDecimal principalPerMonth =
                loan.getPrincipalAmount()
                        .divide(BigDecimal.valueOf(tenor), 2, RoundingMode.HALF_UP);

        BigDecimal interestPerMonth =
                loan.getTotalInterest()
                        .divide(BigDecimal.valueOf(tenor), 2, RoundingMode.HALF_UP);

        BigDecimal accumulatedPrincipal = BigDecimal.ZERO;
        BigDecimal accumulatedInterest = BigDecimal.ZERO;

        List<LoanInstallment> installments = new ArrayList<>();

        for (int i = 1; i <= tenor; i++) {

            BigDecimal principal = principalPerMonth;
            BigDecimal interest = interestPerMonth;

            // Fix rounding on last installment
            if (i == tenor) {
                principal = loan.getPrincipalAmount().subtract(accumulatedPrincipal);
                interest = loan.getTotalInterest().subtract(accumulatedInterest);
            }

            LocalDate dueDate =
                    loan.getDisbursementDate().plusMonths(i);

            LoanInstallment installment =
                    LoanInstallment.create(
                            loan.getId(),
                            i,
                            principal,
                            interest,
                            dueDate
                    );

            installments.add(installment);

            accumulatedPrincipal = accumulatedPrincipal.add(principal);
            accumulatedInterest = accumulatedInterest.add(interest);
        }

        return installments;
    }
}