ALTER TABLE weekly_availabilities ADD COLUMN IF NOT EXISTS conflicts_only BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE weekly_availabilities w SET conflicts_only=TRUE
WHERE w.notes LIKE '[CSV conflicts only]%'
AND EXISTS (SELECT 1 FROM applications a WHERE a.applicant_id=w.applicant_id AND a.period_id=w.period_id AND a.qualification_snapshot->>'importFingerprint' IS NOT NULL);
UPDATE applications SET availability_snapshot=jsonb_set(availability_snapshot,'{conflictsOnly}','true'::jsonb)
WHERE qualification_snapshot->>'importFingerprint' IS NOT NULL AND availability_snapshot->>'notes' LIKE '[CSV conflicts only]%';
