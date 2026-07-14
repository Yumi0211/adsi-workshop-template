CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    type VARCHAR(30) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    acknowledged BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_alerts_employee_acknowledged ON alerts(employee_id, acknowledged);
CREATE INDEX idx_alerts_type_acknowledged ON alerts(type, acknowledged);
