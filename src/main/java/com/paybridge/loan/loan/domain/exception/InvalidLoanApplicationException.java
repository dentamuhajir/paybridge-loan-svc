package com.paybridge.loan.loan.domain.exception;

import com.paybridge.loan.shared.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidLoanApplicationException extends BusinessException {
    public InvalidLoanApplicationException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
