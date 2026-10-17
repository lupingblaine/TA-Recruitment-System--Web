ALTER TABLE section_assignments
    ADD COLUMN response_deadline TEXT,
    ADD COLUMN expired_at TEXT;
-- Existing published offers get a full seven-day grace period at upgrade time.
UPDATE section_assignments SET response_deadline = to_char((now() + interval '7 days') AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS.US"Z"')
    WHERE publication_status = 'published' AND response_status = 'pending' AND status = 'active';
ALTER TABLE section_assignments DROP CONSTRAINT section_assignments_response_status_check;
ALTER TABLE section_assignments ADD CONSTRAINT section_assignments_response_status_check
    CHECK (response_status IN ('pending','accepted','declined','expired'));
ALTER TABLE section_assignments DROP CONSTRAINT assignment_response_metadata;
ALTER TABLE section_assignments ADD CONSTRAINT assignment_response_metadata
    CHECK (response_status IN ('pending','expired') OR (publication_status = 'published' AND responded_at IS NOT NULL));
ALTER TABLE section_assignments ADD CONSTRAINT assignment_pending_deadline
    CHECK (status <> 'active' OR publication_status <> 'published' OR response_status <> 'pending' OR response_deadline IS NOT NULL);
ALTER TABLE section_assignments ADD CONSTRAINT assignment_expiry_metadata
    CHECK (response_status <> 'expired' OR (status = 'cancelled' AND publication_status = 'published' AND response_deadline IS NOT NULL AND expired_at IS NOT NULL AND responded_at IS NULL));
