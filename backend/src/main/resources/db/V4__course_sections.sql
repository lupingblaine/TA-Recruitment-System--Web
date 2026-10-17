CREATE TABLE course_sections (
    id TEXT PRIMARY KEY,
    module_id TEXT NOT NULL REFERENCES modules(id) DEFERRABLE INITIALLY DEFERRED,
    name TEXT NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('lecture','lab','discussion')),
    time_zone TEXT NOT NULL CHECK (time_zone = 'America/Los_Angeles'),
    meeting_times JSONB NOT NULL CHECK (jsonb_typeof(meeting_times) = 'array' AND jsonb_array_length(meeting_times) > 0),
    ta_positions INTEGER NOT NULL CHECK (ta_positions BETWEEN 0 AND 1000),
    hours_per_week DOUBLE PRECISION NOT NULL CHECK (hours_per_week BETWEEN 0 AND 168),
    location TEXT NOT NULL,
    notes TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('active','archived')),
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    row_order INTEGER NOT NULL,
    CHECK ((ta_positions = 0 AND hours_per_week = 0) OR (ta_positions > 0 AND hours_per_week > 0))
);
CREATE UNIQUE INDEX course_sections_module_name_unique ON course_sections(module_id, lower(name));
