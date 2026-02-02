package com.paybridge.loan.loan.application.service;

import com.paybridge.loan.loan.application.command.CreateLoanApplicationCommand;
import com.paybridge.loan.loan.application.port.product.ProductClient;
import com.paybridge.loan.loan.domain.exception.InvalidLoanApplicationException;
import com.paybridge.loan.loan.domain.exception.InvalidLoanException;
import com.paybridge.loan.loan.domain.model.Loan;
import com.paybridge.loan.loan.domain.model.LoanApplication;
import com.paybridge.loan.loan.domain.model.ProductTenor;
import com.paybridge.loan.loan.domain.policy.DisbursementDateCalculator;
import com.paybridge.loan.loan.domain.policy.InterestCalculator;
import com.paybridge.loan.loan.infrastructure.persistence.LoanApplicationRepository;
import com.paybridge.loan.loan.infrastructure.persistence.LoanRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class LoanApplicationService {
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanRepository loanRepository;
    private final ProductClient productClient;
    private final InterestCalculator interestCalculator;
    private final DisbursementDateCalculator disbursementDateCalculator;

    public LoanApplicationService(
            LoanApplicationRepository loanApplicationRepository,
            LoanRepository loanRepository,
            ProductClient productClient,
            InterestCalculator interestCalculator,
            DisbursementDateCalculator disbursementDateCalculator
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanRepository = loanRepository;
        this.productClient = productClient;
        this.interestCalculator = interestCalculator;
        this.disbursementDateCalculator = disbursementDateCalculator;
    }

    public LoanApplication apply(CreateLoanApplicationCommand command) {

        LoanApplication loanApplication = LoanApplication.submit(
                command.userId(),
                command.productId(),
                command.loanProductId(),
                command.loanTenorId(),
                command.interestRate(),
                command.adminFee(),
                command.requestedAmount()
        );

        return loanApplicationRepository.save(loanApplication);
    }

    @Transactional
    public void approveAndCreateLoan(UUID loanApplicationId) {
        LoanApplication loanApplication = approve(loanApplicationId);
        ProductTenor productTenor = productClient.getLoanTenor(loanApplication.getLoanTenorId());
        createLoan(loanApplication, productTenor);
    }

    public LoanApplication approve(UUID loanApplicationId) {
        LoanApplication loanApplication = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() ->
                        new InvalidLoanApplicationException("Loan application not found")
                );
        loanApplication.approve();
        return loanApplicationRepository.save(loanApplication);
    }

    public void createLoan(LoanApplication loanApplication, ProductTenor productTenor){
        if(loanRepository.existsByLoanApplicationId(loanApplication.getId())) {
            throw  new InvalidLoanException("Loan already exists for this application");
        }

        BigDecimal totalInterest =
                interestCalculator.calculate(
                        loanApplication.getRequestedAmount(),
                        productTenor.interestType(),
                        productTenor.interestRate(),
                        productTenor.tenorMonths()
                );

        LocalDate disbursementDate = disbursementDateCalculator.calculate(loanApplication.getApprovedAt());

        Loan loan = Loan.create(
                loanApplication.getId(),
                loanApplication.getUserId(),
                loanApplication.getRequestedAmount(),
                productTenor.interestType(),
                productTenor.interestRate(),
                productTenor.tenorMonths(),
                totalInterest,
                disbursementDate
        );

        loanRepository.save(loan);
    }
}
