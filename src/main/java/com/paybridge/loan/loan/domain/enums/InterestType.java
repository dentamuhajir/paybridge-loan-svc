package com.paybridge.loan.loan.domain.enums;

import com.paybridge.loan.loan.domain.exception.InvalidLoanException;

public enum InterestType {
    FLAT,
    ANNUITY;

    public static InterestType fromExternal(String value) {
        if (value == null) {
            throw new InvalidLoanException("Interest type cannot be null");
        }

        try {
            return InterestType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidLoanException(
                    "Unsupported interest type from product service: " + value
            );
        }
    }
}
