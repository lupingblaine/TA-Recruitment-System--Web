CREATE TABLE weekly_availabilities (
    id TEXT PRIMARY KEY,
    applicant_id TEXT NOT NULL REFERENCES users(id) DEFERRABLE INITIALLY DEFERRED,
    period_id TEXT NOT NULL REFERENCES application_periods(id) DEFERRABLE INITIALLY DEFERRED,
    time_zone TEXT NOT NULL CHECK (time_zone = 'America/Los_Angeles'),
    available_slots JSONB NOT NULL CHECK (jsonb_typeof(available_slots) = 'array'),
    conflict_slots JSONB NOT NULL CHECK (jsonb_typeof(conflict_slots) = 'array'),
    no_availability BOOLEAN NOT NULL,
    notes TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    row_order INTEGER NOT NULL,
    UNIQUE (applicant_id, period_id)
);
ALTER TABLE applications ADD COLUMN availability_snapshot JSONB;
