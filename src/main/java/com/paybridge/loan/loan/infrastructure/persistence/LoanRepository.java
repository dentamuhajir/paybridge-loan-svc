package com.paybridge.loan.loan.infrastructure.persistence;

import com.paybridge.loan.loan.domain.enums.LoanStatus;
import com.paybridge.loan.loan.domain.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {
    boolean existsByLoanApplicationId(UUID loanApplicationId);

    @Query("""
        SELECT l
        FROM Loan l
        WHERE l.status = :status
          AND l.disbursementDate = :today
          AND l.disbursedAt IS NULL
    """)
    List<Loan> findLoansForAutoDisbursement(
            LoanStatus status,
            LocalDate today
    );
}

