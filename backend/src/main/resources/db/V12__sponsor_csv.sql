ALTER TABLE course_sections ADD COLUMN IF NOT EXISTS matching_criteria JSONB;
ALTER TABLE review_records ADD COLUMN IF NOT EXISTS section_id TEXT REFERENCES course_sections(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE review_records ADD COLUMN IF NOT EXISTS source_metadata JSONB;
