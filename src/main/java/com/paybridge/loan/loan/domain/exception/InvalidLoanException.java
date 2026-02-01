package com.paybridge.loan.loan.domain.exception;

import com.paybridge.loan.shared.exception.BusinessException;

public class InvalidLoanException extends BusinessException {
    public InvalidLoanException(String message) {
        super(message);
    }
}
