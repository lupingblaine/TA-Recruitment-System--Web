# Teacher Direct Selection Implementation Plan

> **For agentic workers:** Use superpowers:subagent-driven-development for independent frontend and CSV/name tasks. Preserve all existing changes. Do not commit or push without user confirmation.

**Goal:** Implement the approved Teacher direct selection, admin-only CSV and consistent role labels.

**Architecture:** A dedicated direct-selection service validates a detached candidate state with existing ReviewService and SectionAssignmentService, then applies both changes under the existing request transaction/lock. Review API exposes permitted selection and a shared form is used by Review Workspace and application details. Internal role keys remain compatible.

**Tech Stack:** Java 17 Servlet/JSP, PostgreSQL, browser JavaScript, JUnit, Node/Chromium fixtures.

### 1. Backend selection (parent)
- [x] Add TeacherSelectionServiceTest using real detached fixtures: teacher default approve; custom revoked grants; own-course success; section capacity, cycle, conflict and duplicate rejection; prior decision no overwrite; failure leaves original data untouched.
- [x] Run Maven targeted tests and confirm missing feature RED.
- [x] Add TeacherSelectionService.select(user,applicationId,body), validating via copied DataStore before updating original. Review approval action then Section assignment in the same request transaction; preserve original on expected validation failure.
- [x] Add POST /review/{applicationId}/select-ta and row canSelectTa plus selectionAssignmentId for existing draft. Catch assignment validation as correct HTTP errors.
- [x] Run targeted services/API and adjust existing default-teacher tests for intentionally approved policy.

### 2. Selection UI (agent)
- [x] Add frontend behavior tests before shared direct-selection JavaScript implementation.
- [x] Render Select as TA only with server canSelectTa, Section choice, 10–4000 reason, explicit pending availability confirmation and note. POST /review/{id}/select-ta. Prevent duplicate submission, keep validation feedback, link draft to TA Assignments.
- [x] Integrate in Review Workspace and organiser application detail with shared form, not separate decision logic.
- [x] Test form success, failure, permission false, pending confirmation and duplicate clicks.

### 3. CSV and labels (agent)
- [x] Add regression proving Teacher CSV service/HTTP access denied while admin works. Confirm RED then restrict CSV service, servlet and filter; remove organiser CSV page/sidebar entry.
- [x] Add central RoleLabels mapping applicant->applicant, organiser->Teacher, admin->admin. Use UI labels only; keep routes/role values and legacy advanced review grants unchanged. Mark advanced reviewer designations explicitly.
- [x] Check auth, common navigation/profile, admin Users/modules/properties, review grants, CSV labels, visible messages and manuals. Do not rewrite historical audit records.
- [x] Run relevant tests; report changed files and remaining labels.

### 4. Verify/document (parent)
- [x] Review integrations, run non-Postgres Java suite and frontend tests, JSP script syntax and diff whitespace checks.
- [x] Request code review; reproduce/fix material findings and rerun affected tests.
- [x] Update approved design status, audit and XinyiXu-change-log.md with actual results and integration limits.
- [x] Present concrete result for user confirmation. No commit/push.

Verification: 408 full non-Postgres Java tests passed; 22 focused selection tests passed after adding the success HTTP actor test; 30 frontend tests passed; 20 inline JSP scripts syntax-checked; git diff --check passed. No commits or pushes.
