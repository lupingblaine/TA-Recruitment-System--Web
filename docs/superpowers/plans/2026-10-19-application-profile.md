# Application profile implementation plan

**Goal:** Make the student application profile easier to complete during the current Design and Foundation phase.

**Architecture:** Retain the JSP and its existing REST endpoints. Use scoped CSS for layout and a single page script for lifecycle, validation and existing payload serialization. Execute inline in the current worktree and preserve previous changes.

**Tech stack:** JSP, browser JavaScript, CSS, Node test runner and bundled Playwright Chromium.

- [x] Add browser regression tests in `tests/frontend/application-profile.test.cjs` using the actual JSP body and script, a synthetic profile and intercepted API requests. Run with `PLAYWRIGHT_MODULE` pointing to the bundled module. Confirm new retry, summary and navigation behavior fails before changes.
- [x] Restructure `backend/src/main/webapp/applicant/application-profile.jsp` into five labelled sections. Add summary, retry and status elements; keep all original input names and acknowledgement text.
- [x] Add `backend/src/main/webapp/static/css/application-profile.css` scoped to this page, with one-column mobile fields and two-column desktop fields and visible focus styles.
- [x] Update `backend/src/main/webapp/static/js/application-profile.js`: load and retry, hydrate safely, graduate-year guidance, validation summary, dirty/save states and original serialization.
- [x] Run `node --check`, browser tests at desktop and 390px/320px widths, existing email tests and Java QualificationService tests. Review screenshots and `git diff --check`.
- [x] Record current-phase tasks and verified evidence in `docs/frontend-current-phase-tasks.md`; describe browser fixture and live integration limits accurately.
