# Interview workflow removal

Interview management was removed from the BioSci prototype on October 27, 2026 because candidate interview scheduling is outside the supplied MVP scope.

## Current workflow

The organiser pipeline contains Applied, Review, Shortlisted, Offer and Rejected. Selection approval remains in Review Workspace; formal Section assignment and student replies remain in TA Assignments / My Assignments. This change does not introduce autonomous selection or change assignment approval rules.

- The Interviews page, navigation entry and scheduling/editing actions are removed.
- The organiser interview GET, POST, PUT and DELETE routes no longer exist and return 404 for authenticated organisers.
- Applicant and organiser dashboards no longer contain interview statistics, interview events or interview-only recent activity. The work calendar continues to show formal assignment duties.
- Pipeline and application detail no longer offer interview scheduling. The organiser dashboard shows recent applications.
- Header and dashboard notifications no longer present historical interview notifications. Other application and assignment notifications are retained.
- Data Editor no longer lists interviews. Direct requests to its interview collection return 410; direct service writes are rejected. It also rejects introducing a new interview status on an application; an unchanged historical status is allowed when editing other application fields.
- Landing-page marketing copy and Markdown manuals no longer advertise interview scheduling.

## Historical compatibility

No database tables, stored interview records or seed fixtures are deleted by this change. The Interview model and PostgreSQL persistence mapping remain solely to load and preserve old data. This is deliberate compatibility code, not an active interview feature.

Stored applications whose status is `interview` are projected as `review` in applicant, organiser and review-facing responses. The organiser pipeline groups them in Review so candidates do not disappear. Retired timeline steps are omitted from detached API projections. Neither operation rewrites stored status or timeline history. Existing interview notification records are retained but filtered from active notification responses.

Normal existing parent-record deletion rules are unchanged; this change does not promise a new archive retention policy. Any later database/schema purge must be handled separately with backups and an explicit migration plan.

Historical Version 1 notes, PDF manuals, generated Javadocs and screenshots may still depict the old workflow. Current source and this note describe the updated behavior; binary documents were not regenerated.

## Verification

Regression coverage includes:

- Removed organiser HTTP routes return 404.
- Dashboards and pipeline do not expose interview stats/events/columns.
- Legacy candidates remain reviewable without rewriting stored state.
- New interview state requests are rejected; archived interview collection writes are rejected.
- Interview notifications are filtered without deleting stored notifications.
- Chromium fixtures render the organiser dashboard/pipeline and applicant dashboard/applications without interview controls, errors or requests.
- Earlier application-profile view/edit/cancel and email-handoff regressions are retained.

Browser checks use actual page scripts and JSP bodies with JSP directives substituted and synthetic API responses intercepted. They do not verify live JSP compilation, live database persistence or production deployment. The separate PostgreSQL integration test suite requires its own database environment.

Final regression results: 343 Java tests passed with zero failures/errors/skips, excluding `PostgresStoreTest` from this run. Seventeen frontend tests passed, including four Interview-removal page checks plus the existing profile and email tests. Inline JavaScript syntax checks passed on ten affected JSP surfaces; `git diff --check` passed. A separate code review found an unchanged-legacy-status editing regression, which was reproduced, fixed and covered by a regression test before the final run.
