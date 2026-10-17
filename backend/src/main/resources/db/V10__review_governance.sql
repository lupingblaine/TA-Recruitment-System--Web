ALTER TABLE users ADD COLUMN IF NOT EXISTS permissions JSONB;
ALTER TABLE users ADD COLUMN IF NOT EXISTS course_ids JSONB;
CREATE TABLE IF NOT EXISTS review_records (
 id TEXT PRIMARY KEY,
 application_id TEXT NOT NULL REFERENCES applications(id) DEFERRABLE INITIALLY DEFERRED,
 actor_id TEXT NOT NULL,
 action TEXT NOT NULL CHECK(action IN ('note','flag','recommend','alternate','approve','not_selected')),
 note TEXT NOT NULL,
 created_at TEXT NOT NULL,
 criteria_snapshot JSONB,
 schedule_snapshot JSONB,
 row_order INTEGER NOT NULL
);
