CREATE TABLE loan_installments (
    id UUID PRIMARY KEY,
    loan_id UUID NOT NULL,
    installment_number INT NOT NULL,
    principal_amount NUMERIC(19,2) NOT NULL,
    interest_amount NUMERIC(19,2) NOT NULL,
    total_amount NUMERIC(19,2) NOT NULL,
    paid_amount NUMERIC(19,2) DEFAULT 0 NOT NULL,
    penalty_amount NUMERIC(19,2) DEFAULT 0 NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_loan_installments_loan
        FOREIGN KEY (loan_id)
        REFERENCES loans(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_loan_installments_loan_id
    ON loan_installments(loan_id);