CREATE TABLE leave_balances (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    fiscal_year INTEGER NOT NULL,
    granted_days DECIMAL(4,1) NOT NULL,
    used_days DECIMAL(4,1) NOT NULL DEFAULT 0,
    carried_over_days DECIMAL(4,1) NOT NULL DEFAULT 0,
    grant_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (employee_id, fiscal_year, grant_date)
);
