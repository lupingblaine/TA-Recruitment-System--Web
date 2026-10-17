ALTER TABLE section_assignments
    ADD COLUMN publication_status TEXT NOT NULL DEFAULT 'draft' CHECK (publication_status IN ('draft','published')),
    ADD COLUMN published_by TEXT,
    ADD COLUMN published_at TEXT,
    ADD COLUMN response_status TEXT NOT NULL DEFAULT 'pending' CHECK (response_status IN ('pending','accepted','declined')),
    ADD COLUMN responded_at TEXT,
    ADD COLUMN response_reason TEXT;
ALTER TABLE section_assignments
    ADD CONSTRAINT assignment_publication_metadata CHECK (publication_status <> 'published' OR (published_by IS NOT NULL AND published_at IS NOT NULL)),
    ADD CONSTRAINT assignment_response_metadata CHECK (response_status = 'pending' OR (publication_status = 'published' AND responded_at IS NOT NULL)),
    ADD CONSTRAINT assignment_decline_releases_capacity CHECK (response_status <> 'declined' OR (status = 'cancelled' AND length(trim(response_reason)) > 0 AND response_reason IS NOT NULL));
