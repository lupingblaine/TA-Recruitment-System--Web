ALTER TABLE profiles ADD COLUMN IF NOT EXISTS qualifications JSONB;
ALTER TABLE applications ADD COLUMN IF NOT EXISTS qualification_snapshot JSONB;
