package com.paybridge.loan.loan.application.service;

import com.paybridge.loan.loan.application.command.CreateLoanApplicationCommand;
import com.paybridge.loan.loan.application.port.product.ProductClient;
import com.paybridge.loan.loan.application.port.transaction.TransactionClient;
import com.paybridge.loan.loan.domain.exception.InvalidLoanApplicationException;
import com.paybridge.loan.loan.domain.exception.InvalidLoanException;
import com.paybridge.loan.loan.domain.model.Account;
import com.paybridge.loan.loan.domain.model.Loan;
import com.paybridge.loan.loan.domain.model.LoanApplication;
import com.paybridge.loan.loan.domain.model.ProductTenor;
import com.paybridge.loan.loan.domain.policy.DisbursementDateCalculator;
import com.paybridge.loan.loan.domain.policy.InstallmentScheduleGenerator;
import com.paybridge.loan.loan.domain.policy.InterestCalculator;
import com.paybridge.loan.loan.infrastructure.persistence.LoanApplicationRepository;
import com.paybridge.loan.loan.infrastructure.persistence.LoanInstallmentRepository;
import com.paybridge.loan.loan.infrastructure.persistence.LoanRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanRepository loanRepository;
    private final ProductClient productClient;
    private final TransactionClient transactionClient;
    private final InterestCalculator interestCalculator;
    private final DisbursementDateCalculator disbursementDateCalculator;
    private final LoanInstallmentRepository loanInstallmentRepository;
    private final InstallmentScheduleGenerator installmentScheduleGenerator;

    public LoanApplicationService(
            LoanApplicationRepository loanApplicationRepository,
            LoanRepository loanRepository,
            LoanInstallmentRepository loanInstallmentRepository,
            ProductClient productClient,
            TransactionClient transactionClient,
            InterestCalculator interestCalculator,
            DisbursementDateCalculator disbursementDateCalculator,
            InstallmentScheduleGenerator installmentScheduleGenerator
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanRepository = loanRepository;
        this.loanInstallmentRepository = loanInstallmentRepository;
        this.productClient = productClient;
        this.transactionClient = transactionClient;
        this.interestCalculator = interestCalculator;
        this.disbursementDateCalculator = disbursementDateCalculator;
        this.installmentScheduleGenerator = installmentScheduleGenerator;
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

    public void approveAndCreateLoan(UUID loanApplicationId) {

        Span span = Span.current();

        try (var ignored = org.slf4j.MDC.putCloseable(
                "loan_application_id", loanApplicationId.toString())) {

            span.setAttribute("loan.application.id", loanApplicationId.toString());
            log.info("starting loan approval process");

            LoanApplication loanApplication = loanApplicationRepository.findById(loanApplicationId)
                    .orElseThrow(() ->
                            new InvalidLoanApplicationException("Loan application not found")
                    );

            ProductTenor productTenor =
                    productClient.getLoanTenor(loanApplication.getLoanTenorId());

            log.info("product tenor fetched");

            Account account =
                    transactionClient.getAccount(loanApplication.getUserId());

            log.info("account verified");

            approveAndCreateTransactional(loanApplicationId, productTenor, account);

            log.info("loan successfully created");
        } catch (Exception e) {

            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());

            log.error("loan approval failed", e);
            throw e;
        }
    }

    @Transactional
    protected void approveAndCreateTransactional(
            UUID loanApplicationId,
            ProductTenor productTenor,
            Account account
    ) {
        log.info("Starting transactional approval for loanApplicationId={}", loanApplicationId);

        LoanApplication loanApplication = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() ->
                        new InvalidLoanApplicationException("Loan application not found")
                );

        log.info("Loan application found. Approving application id={}", loanApplicationId);

        loanApplication.approve();

        if (loanRepository.existsByLoanApplicationId(loanApplication.getId())) {
            log.warn("Loan already exists for loanApplicationId={}", loanApplicationId);
            throw new InvalidLoanException("Loan already exists for this application");
        }

        log.info(
                "Calculating interest for application id={} (amount={}, rate={}, tenor={}, type={})",
                loanApplicationId,
                loanApplication.getRequestedAmount(),
                productTenor.interestRate(),
                productTenor.tenorMonths(),
                productTenor.interestType()
        );

        BigDecimal totalInterest =
                interestCalculator.calculate(
                        loanApplication.getRequestedAmount(),
                        productTenor.interestType(),
                        productTenor.interestRate(),
                        productTenor.tenorMonths()
                );

        log.info(
                "Interest calculated for application id={} → totalInterest={}",
                loanApplicationId,
                totalInterest
        );

        LocalDate disbursementDate =
                disbursementDateCalculator.calculate(loanApplication.getApprovedAt());

        log.info(
                "Disbursement date calculated for application id={} → {}",
                loanApplicationId,
                disbursementDate
        );

        Loan loan = Loan.create(
                loanApplication.getId(),
                loanApplication.getUserId(),
                loanApplication.getRequestedAmount(),
                productTenor.interestType(),
                productTenor.interestRate(),
                productTenor.tenorMonths(),
                totalInterest,
                disbursementDate,
                account.ownerId()
        );

        loanRepository.save(loan);

        log.info(
                "Loan created successfully. loanId={}, totalPayable={}, status={}",
                loan.getId(),
                loan.getTotalPayable(),
                loan.getStatus()
        );

        log.info(
                "Generating {} installments for loanId={}",
                loan.getTenorMonths(),
                loan.getId()
        );

        var installments = installmentScheduleGenerator.generate(loan);
        loanInstallmentRepository.saveAll(installments);
        log.info(
                "Installments generated and saved successfully for loanId={} (count={})",
                loan.getId(),
                installments.size()
        );

        log.info("Transactional approval completed successfully for loanApplicationId={}", loanApplicationId);
    }
}
