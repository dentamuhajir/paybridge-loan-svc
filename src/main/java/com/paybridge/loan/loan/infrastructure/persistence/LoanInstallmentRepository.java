package com.paybridge.loan.loan.infrastructure.persistence;

import com.paybridge.loan.loan.domain.model.LoanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanInstallmentRepository
        extends JpaRepository<LoanInstallment, UUID> {

    List<LoanInstallment> findByLoanIdOrderByInstallmentNumber(UUID loanId);
}