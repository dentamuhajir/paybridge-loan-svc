package com.paybridge.loan.loan.domain.model;

import com.paybridge.loan.loan.domain.enums.InterestType;
import com.paybridge.loan.loan.domain.enums.LoanStatus;
import com.paybridge.loan.loan.domain.exception.InvalidLoanException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "loans")
@Setter
@Getter
public class Loan {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "loan_application_id", nullable = false)
    private UUID loanApplicationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "principal_amount", nullable = false)
    private BigDecimal principalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_type", nullable = false)
    private InterestType interestType;

    @Column(name = "interest_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal interestRate;

    @Column(name = "tenor_months", nullable = false)
    private int tenorMonths;

    @Column(name = "total_interest", nullable = false)
    private BigDecimal totalInterest;

    @Column(name = "total_payable", nullable = false)
    private BigDecimal totalPayable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    @Column(name = "disbursed_at")
    private Instant disbursedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Loan() {}

    //  Factory method (create loan = business concept)
    public static Loan create(
            UUID loanApplicationId,
            UUID userId,
            BigDecimal principalAmount,
            InterestType interestType,
            BigDecimal interestRate,
            int tenorMonths,
            BigDecimal totalInterest,
            BigDecimal totalPayable,
            LocalDate disbursementDate
    ) {
        Loan loan = new Loan();
        loan.id = UUID.randomUUID();
        loan.loanApplicationId = loanApplicationId;
        loan.userId = userId;
        loan.principalAmount = principalAmount;
        loan.interestType = interestType;
        loan.interestRate = interestRate;
        loan.tenorMonths = tenorMonths;
        loan.totalInterest = totalInterest;
        loan.totalPayable = totalPayable;
        loan.disbursementDate = disbursementDate;
        loan.status = LoanStatus.CREATED;
        loan.createdAt = Instant.now();
        return loan;
    }

    // future use
    public void activate() {
        if (this.status != LoanStatus.CREATED) {
            throw new InvalidLoanException(
                    "Only CREATED loan can be activated"
            );
        }
        this.status = LoanStatus.ACTIVE;
        this.disbursedAt = Instant.now();
    }
}
