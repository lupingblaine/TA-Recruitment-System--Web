CREATE TABLE application_periods (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    term_id TEXT REFERENCES terms(id) DEFERRABLE INITIALLY DEFERRED,
    opens_at TEXT NOT NULL,
    closes_at TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('draft', 'open', 'closed')),
    legacy BOOLEAN NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    row_order INTEGER NOT NULL,
    CHECK (opens_at::timestamptz < closes_at::timestamptz)
);
CREATE UNIQUE INDEX application_periods_one_open ON application_periods (status) WHERE status = 'open';
ALTER TABLE applications ADD COLUMN period_id TEXT;
ALTER TABLE applications ADD CONSTRAINT applications_period_id_fk FOREIGN KEY (period_id)
    REFERENCES application_periods(id) DEFERRABLE INITIALLY DEFERRED;
CREATE INDEX applications_period_id_idx ON applications(period_id);
