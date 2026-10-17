ALTER TABLE course_sections ADD COLUMN IF NOT EXISTS period_id TEXT REFERENCES application_periods(id) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE course_sections ADD COLUMN IF NOT EXISTS faculty_name TEXT;
DROP INDEX IF EXISTS course_sections_module_name_unique;
CREATE UNIQUE INDEX IF NOT EXISTS course_sections_period_name_unique ON course_sections(module_id,COALESCE(period_id,''),lower(name));
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS period_criteria JSONB;
