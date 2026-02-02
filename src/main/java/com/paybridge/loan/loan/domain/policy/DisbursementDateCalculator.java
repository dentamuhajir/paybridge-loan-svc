package com.paybridge.loan.loan.domain.policy;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class DisbursementDateCalculator {
    public LocalDate calculate(Instant approvedAt) {
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
