CREATE TABLE time_records (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    record_date DATE NOT NULL,
    type VARCHAR(20) NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    source VARCHAR(10) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_time_records_employee_date ON time_records(employee_id, record_date);
