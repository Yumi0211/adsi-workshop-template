CREATE TABLE work_calendars (
    id BIGSERIAL PRIMARY KEY,
    calendar_date DATE NOT NULL UNIQUE,
    day_type VARCHAR(20) NOT NULL,
    description VARCHAR(100),
    fiscal_year INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_work_calendars_fiscal_year ON work_calendars(fiscal_year);
