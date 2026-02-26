package com.paybridge.loan.loan.domain.model;

import com.paybridge.loan.loan.domain.enums.InstallmentStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "loan_installments",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"loan_id", "installment_number"}
        )
)
@Getter
public class LoanInstallment {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "loan_id", nullable = false)
    private UUID loanId;

    @Column(name = "installment_number", nullable = false)
    private int installmentNumber;

    @Column(name = "principal_amount", nullable = false)
    private BigDecimal principalAmount;

    @Column(name = "interest_amount", nullable = false)
    private BigDecimal interestAmount;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", nullable = false)
    private BigDecimal paidAmount;

    @Column(name = "penalty_amount", nullable = false)
    private BigDecimal penaltyAmount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstallmentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LoanInstallment() {}

    public static LoanInstallment create(
            UUID loanId,
            int installmentNumber,
            BigDecimal principalAmount,
            BigDecimal interestAmount,
            LocalDate dueDate
    ) {

        LoanInstallment installment = new LoanInstallment();
        installment.id = UUID.randomUUID();
        installment.loanId = loanId;
        installment.installmentNumber = installmentNumber;
        installment.principalAmount = principalAmount;
        installment.interestAmount = interestAmount;
        installment.totalAmount = principalAmount.add(interestAmount);
        installment.paidAmount = BigDecimal.ZERO;
        installment.penaltyAmount = BigDecimal.ZERO;
        installment.dueDate = dueDate;
        installment.status = InstallmentStatus.PENDING;
        installment.createdAt = Instant.now();

        return installment;
    }
}