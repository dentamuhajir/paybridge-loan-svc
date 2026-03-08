package com.paybridge.loan.loan.application.scheduler;

import com.paybridge.loan.loan.application.service.LoanDisbursementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoanDisbursementScheduler {

    private final LoanDisbursementService loanDisbursementService;

    public LoanDisbursementScheduler(LoanDisbursementService loanDisbursementService) {
        this.loanDisbursementService = loanDisbursementService;
    }

    @Scheduled(cron = "5 * * * * ?")
    public void runAutoDisbursement() {
        log.info("[Scheduler] Starting auto disbursement job");

        try {
            loanDisbursementService.autoDisbursement();
        } catch (Exception e) {
            log.error("[Scheduler] Auto disbursement job failed", e);
        }

        log.info("[Scheduler] Finished auto disbursement job");
    }
}
