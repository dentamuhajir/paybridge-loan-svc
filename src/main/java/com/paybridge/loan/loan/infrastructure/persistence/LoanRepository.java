package com.paybridge.loan.loan.infrastructure.persistence;

import com.paybridge.loan.loan.domain.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {
    boolean existsByLoanApplicationId(UUID loanApplicationId);
}

