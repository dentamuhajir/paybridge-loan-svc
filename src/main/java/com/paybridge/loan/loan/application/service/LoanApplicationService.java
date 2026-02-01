package com.paybridge.loan.loan.application.service;

import com.paybridge.loan.loan.application.command.CreateLoanApplicationCommand;
import com.paybridge.loan.loan.application.port.product.ProductClient;
import com.paybridge.loan.loan.domain.exception.InvalidLoanApplicationException;
import com.paybridge.loan.loan.domain.exception.InvalidLoanException;
import com.paybridge.loan.loan.domain.model.Loan;
import com.paybridge.loan.loan.domain.model.LoanApplication;
import com.paybridge.loan.loan.domain.model.ProductTenor;
import com.paybridge.loan.loan.domain.service.InterestCalculator;
import com.paybridge.loan.loan.infrastructure.persistence.LoanApplicationRepository;
import com.paybridge.loan.loan.infrastructure.persistence.LoanRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Service
public class LoanApplicationService {
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanRepository loanRepository;
    private final ProductClient productClient;
    private final InterestCalculator interestCalculator;

    public LoanApplicationService(
            LoanApplicationRepository loanApplicationRepository,
            LoanRepository loanRepository,
            ProductClient productClient,
            InterestCalculator interestCalculator
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanRepository = loanRepository;
        this.productClient = productClient;
        this.interestCalculator = interestCalculator;
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

        // total payable = (pokok + bunga)
        BigDecimal totalPayable =
                loanApplication.getRequestedAmount().add(totalInterest);

        LocalDate disbursementDate = calculateDisbursementDate(loanApplication.getApprovedAt());

        Loan loan = Loan.create(
                loanApplication.getId(),
                loanApplication.getUserId(),
                loanApplication.getRequestedAmount(),
                productTenor.interestType(),
                productTenor.interestRate(),
                productTenor.tenorMonths(),
                totalInterest,
                totalPayable,
                disbursementDate
        );

        loanRepository.save(loan);
    }

    private LocalDate calculateDisbursementDate(Instant approvedAt) {
        LocalDate approvedDate =
                approvedAt.atZone(ZoneId.systemDefault()).toLocalDate();

        DayOfWeek day = approvedDate.getDayOfWeek();

        return switch (day) {
            case FRIDAY -> approvedDate.plusDays(3);   // Fri -> Mon
            case SATURDAY -> approvedDate.plusDays(2); // Sat -> Mon
            case SUNDAY -> approvedDate.plusDays(1);   // Sun -> Mon
            default -> approvedDate.plusDays(1);       // Mon–Thu -> next day
        };
    }
}
