package com.paybridge.loan.loan.application.service;

import com.paybridge.loan.loan.domain.enums.LoanStatus;
import com.paybridge.loan.loan.domain.model.Loan;
import com.paybridge.loan.loan.infrastructure.persistence.LoanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class LoanDisbursementService {
    private final LoanRepository loanRepository;

    public LoanDisbursementService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public void autoDisbursement() {
        LocalDate today = LocalDate.now();
        List<Loan> loans = loanRepository.findLoansForAutoDisbursement(LoanStatus.CREATED, today);

        log.info("[Service] Total loan for disburse " + loans.size());
        for(Loan loan : loans) {
            disburse(loan);
        }
    }

    public void disburse(Loan loan) {
        loan.activate();
        loanRepository.save(loan);
        // update ledger
    }


}
