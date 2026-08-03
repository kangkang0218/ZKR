CREATE TABLE finance_report_prompt (
    id BIGSERIAL PRIMARY KEY,
    prompt_text TEXT NOT NULL,
    creator_user_id VARCHAR(64),
    creator_name VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_finance_report_prompt_created_at ON finance_report_prompt (created_at DESC);
