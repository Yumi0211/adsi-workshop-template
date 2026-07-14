CREATE TABLE approval_requests (
    id BIGSERIAL PRIMARY KEY,
    applicant_id BIGINT NOT NULL REFERENCES employees(id),
    approver_id BIGINT NOT NULL REFERENCES employees(id),
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    request_date DATE NOT NULL,
    detail JSONB NOT NULL,
    reason TEXT,
    approved_at TIMESTAMP WITH TIME ZONE,
    rejection_reason TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_approval_requests_approver_status ON approval_requests(approver_id, status);
CREATE INDEX idx_approval_requests_applicant ON approval_requests(applicant_id);
