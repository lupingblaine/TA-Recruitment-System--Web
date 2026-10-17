-- V1: scalar fields are SQL columns; nested lists retain their JSON structure.

CREATE TABLE IF NOT EXISTS tamo_schema_version (version INTEGER PRIMARY KEY, installed_at TIMESTAMPTZ NOT NULL DEFAULT now());

CREATE TABLE IF NOT EXISTS users (
    "id" TEXT PRIMARY KEY,
    "name" TEXT,
    "email" TEXT,
    "password" TEXT,
    "phone" TEXT,
    "role" TEXT,
    "department" TEXT,
    "avatar" TEXT,
    "status" TEXT,
    "last_login" TEXT,
    "created_at" TEXT,
    row_order INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS profiles (
    "id" TEXT PRIMARY KEY,
    "user_id" TEXT,
    "student_id" TEXT,
    "degree" TEXT,
    "year" INTEGER,
    "gpa" DOUBLE PRECISION,
    "max_gpa" DOUBLE PRECISION,
    "profile_completion" INTEGER,
    "skills" JSONB,
    "coursework" JSONB,
    "experience" JSONB,
    "documents" JSONB,
    "max_weekly_hours" INTEGER,
    "current_weekly_hours" INTEGER,
    row_order INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS modules (
    "id" TEXT PRIMARY KEY,
    "code" TEXT,
    "name" TEXT,
    "department" TEXT,
    "organiser_id" TEXT,
    "organiser_name" TEXT,
    "term" TEXT,
    "status" TEXT,
    row_order INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS jobs (
    "id" TEXT PRIMARY KEY,
    "module_id" TEXT,
    "module_code" TEXT,
    "title" TEXT,
    "description" TEXT,
    "professor" TEXT,
    "department" TEXT,
    "type" TEXT,
    "hours_per_week" INTEGER,
    "positions" INTEGER,
    "deadline" TEXT,
    "status" TEXT,
    "tags" JSONB,
    "requirements" JSONB,
    "responsibilities" JSONB,
    "organiser_id" TEXT,
    "created_at" TEXT,
    "work_dates" JSONB,
    row_order INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS applications (
    "id" TEXT PRIMARY KEY,
    "job_id" TEXT,
    "applicant_id" TEXT,
    "status" TEXT,
    "cover_letter" TEXT,
    "document_ids" JSONB,
    "match_score" INTEGER,
    "risk_flag" TEXT,
    "ai_summary" TEXT,
    "applied_at" TEXT,
    "updated_at" TEXT,
    "timeline" JSONB,
    row_order INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS interviews (
    "id" TEXT PRIMARY KEY,
    "application_id" TEXT,
    "candidate_id" TEXT,
    "candidate_name" TEXT,
    "module_code" TEXT,
    "module_name" TEXT,
    "date" TEXT,
    "start_time" TEXT,
    "end_time" TEXT,
    "type" TEXT,
    "location" TEXT,
    "status" TEXT,
    "outcome" TEXT,
    "organiser_id" TEXT,
    row_order INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS notifications (
    "id" TEXT PRIMARY KEY,
    "user_id" TEXT,
    "type" TEXT,
    "title" TEXT,
    "message" TEXT,
    "related_id" TEXT,
    "created_at" TEXT,
    "read" BOOLEAN,
    row_order INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS terms (
    "id" TEXT PRIMARY KEY,
    "name" TEXT,
    "start_date" TEXT,
    "end_date" TEXT,
    "status" TEXT,
    row_order INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS departments (
    "id" TEXT PRIMARY KEY,
    "name" TEXT,
    "module_count" INTEGER,
    "status" TEXT,
    row_order INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS logs (
    "id" TEXT PRIMARY KEY,
    "timestamp" TEXT,
    "action" TEXT,
    "actor" TEXT,
    "actor_id" TEXT,
    "type" TEXT,
    "details" TEXT,
    "ip" TEXT,
    row_order INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS workload_assignments (
    "id" TEXT PRIMARY KEY,
    "ta_user_id" TEXT,
    "module_code" TEXT,
    "hours_per_week" INTEGER,
    "term" TEXT,
    "status" TEXT,
    row_order INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS messages (
    "id" TEXT PRIMARY KEY,
    "conversation_id" TEXT,
    "sender_id" TEXT,
    "receiver_id" TEXT,
    "content" TEXT,
    "timestamp" TEXT,
    "read" BOOLEAN,
    "reply_to_id" TEXT,
    "recalled" BOOLEAN,
    "edited" BOOLEAN,
    "edited_at" TEXT,
    "message_type" TEXT,
    "attachment_url" TEXT,
    "attachment_name" TEXT,
    "attachment_size" TEXT,
    row_order INTEGER NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS users_email_unique ON users (lower(email));

ALTER TABLE profiles ADD CONSTRAINT profiles_user_id_fk FOREIGN KEY (user_id) REFERENCES users(id) DEFERRABLE INITIALLY DEFERRED;

CREATE INDEX profiles_user_id_idx ON profiles (user_id);

ALTER TABLE modules ADD CONSTRAINT modules_organiser_id_fk FOREIGN KEY (organiser_id) REFERENCES users(id) DEFERRABLE INITIALLY DEFERRED;

CREATE INDEX modules_organiser_id_idx ON modules (organiser_id);

ALTER TABLE jobs ADD CONSTRAINT jobs_module_id_fk FOREIGN KEY (module_id) REFERENCES modules(id) DEFERRABLE INITIALLY DEFERRED;

CREATE INDEX jobs_module_id_idx ON jobs (module_id);

ALTER TABLE jobs ADD CONSTRAINT jobs_organiser_id_fk FOREIGN KEY (organiser_id) REFERENCES users(id) DEFERRABLE INITIALLY DEFERRED;

CREATE INDEX jobs_organiser_id_idx ON jobs (organiser_id);

ALTER TABLE applications ADD CONSTRAINT applications_job_id_fk FOREIGN KEY (job_id) REFERENCES jobs(id) DEFERRABLE INITIALLY DEFERRED;

CREATE INDEX applications_job_id_idx ON applications (job_id);

ALTER TABLE applications ADD CONSTRAINT applications_applicant_id_fk FOREIGN KEY (applicant_id) REFERENCES users(id) DEFERRABLE INITIALLY DEFERRED;

CREATE INDEX applications_applicant_id_idx ON applications (applicant_id);

ALTER TABLE interviews ADD CONSTRAINT interviews_application_id_fk FOREIGN KEY (application_id) REFERENCES applications(id) DEFERRABLE INITIALLY DEFERRED;

CREATE INDEX interviews_application_id_idx ON interviews (application_id);

ALTER TABLE interviews ADD CONSTRAINT interviews_candidate_id_fk FOREIGN KEY (candidate_id) REFERENCES users(id) DEFERRABLE INITIALLY DEFERRED;

CREATE INDEX interviews_candidate_id_idx ON interviews (candidate_id);

ALTER TABLE interviews ADD CONSTRAINT interviews_organiser_id_fk FOREIGN KEY (organiser_id) REFERENCES users(id) DEFERRABLE INITIALLY DEFERRED;

CREATE INDEX interviews_organiser_id_idx ON interviews (organiser_id);

ALTER TABLE notifications ADD CONSTRAINT notifications_user_id_fk FOREIGN KEY (user_id) REFERENCES users(id) DEFERRABLE INITIALLY DEFERRED;

CREATE INDEX notifications_user_id_idx ON notifications (user_id);

ALTER TABLE workload_assignments ADD CONSTRAINT workload_assignments_ta_user_id_fk FOREIGN KEY (ta_user_id) REFERENCES users(id) DEFERRABLE INITIALLY DEFERRED;

CREATE INDEX workload_assignments_ta_user_id_idx ON workload_assignments (ta_user_id);

ALTER TABLE messages ADD CONSTRAINT messages_sender_id_fk FOREIGN KEY (sender_id) REFERENCES users(id) DEFERRABLE INITIALLY DEFERRED;

CREATE INDEX messages_sender_id_idx ON messages (sender_id);

ALTER TABLE messages ADD CONSTRAINT messages_receiver_id_fk FOREIGN KEY (receiver_id) REFERENCES users(id) DEFERRABLE INITIALLY DEFERRED;

CREATE INDEX messages_receiver_id_idx ON messages (receiver_id);
