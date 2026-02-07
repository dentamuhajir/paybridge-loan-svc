package com.paybridge.loan.loan.infrastructure.client.transaction.dto;

public record TransactionApiResponse<T>(
        boolean success,
        String message,
        T data
) {}