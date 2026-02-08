package com.paybridge.loan.loan.domain.exception;

import com.paybridge.loan.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidLoanException extends BusinessException {
    public InvalidLoanException(String message) {
        super(
                message,
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }
}
