CREATE TABLE section_assignments (
    id TEXT PRIMARY KEY,
    application_id TEXT NOT NULL REFERENCES applications(id) DEFERRABLE INITIALLY DEFERRED,
    section_id TEXT NOT NULL REFERENCES course_sections(id) DEFERRABLE INITIALLY DEFERRED,
    applicant_id TEXT NOT NULL REFERENCES users(id) DEFERRABLE INITIALLY DEFERRED,
    period_id TEXT NOT NULL REFERENCES application_periods(id) DEFERRABLE INITIALLY DEFERRED,
    status TEXT NOT NULL CHECK (status IN ('active','cancelled')),
    assigned_by TEXT NOT NULL,
    assigned_at TEXT NOT NULL,
    cancelled_by TEXT,
    cancelled_at TEXT,
    cancellation_reason TEXT,
    manual_confirmation_note TEXT,
    section_snapshot JSONB NOT NULL CHECK (jsonb_typeof(section_snapshot) = 'object'),
    row_order INTEGER NOT NULL,
    CHECK (status <> 'cancelled' OR (cancelled_by IS NOT NULL AND cancelled_at IS NOT NULL AND length(cancellation_reason) > 0))
);
CREATE UNIQUE INDEX section_assignments_active_unique
    ON section_assignments(section_id, applicant_id, period_id) WHERE status = 'active';
CREATE INDEX section_assignments_student_period ON section_assignments(applicant_id,period_id);
