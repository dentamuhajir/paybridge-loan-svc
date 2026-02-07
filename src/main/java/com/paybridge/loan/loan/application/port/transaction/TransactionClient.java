package com.paybridge.loan.loan.application.port.transaction;

import com.paybridge.loan.loan.domain.model.Account;

import java.util.UUID;

public interface TransactionClient {
    Account getAccount(UUID ownerId);
}
