# Frontend development tasks for the current phase

**Current phase:** Design and Foundation, Weeks 3–4, October 12–23, 2026.
**Current week:** October 19–23, 2026.
**Focus:** Student application profile and the related student-facing account and navigation experience in the existing TA&MO JSP/Servlet application.

## Submission text

I am involved in the following frontend development tasks for the current Design and Foundation phase:

1. **Student TA application profile layout and navigation**
   - Map the existing application fields into five sections: identity and academics, experience and training, skills and course interests, scheduling and arrangements, and acknowledgements.
   - Implement section links with keyboard navigation and focus management.
   - Show saved details as text by default; enter the form with Edit, return to the display after saving, and restore saved values when cancelling.
   - Refine the layout for desktop and mobile screens while retaining existing form fields and API contracts.
2. **Application form guidance and validation**
   - Label required and optional fields and provide guidance for UCI email addresses and eight-digit student IDs.
   - Update the graduate start-year requirement and instructions when the student level changes.
   - Provide an accessible validation summary with links to fields that need correction.
   - Clarify that saving a profile does not submit a job application and that existing submissions retain their historical data.
3. **Profile loading and saving interactions**
   - Show loading, unsaved-change, saving, success and error states.
   - Add a retry action after an initial loading failure.
   - Prevent duplicate saves and preserve entered information after a failed save.
   - Display skills and course choices safely and provide guidance when options are unavailable.
4. **Related student account and navigation fixes**
   - Carry the email entered on the login page to the Create Account page.
   - Remove the staff CSV menu from the applicant interface and coordinate server-side restrictions on staff pages and CSV operations.
   - Keep My Assignments focused on the student's own published arrangements, rather than staff assignment management.
5. **Frontend verification and integration review**
   - Add browser regression tests using synthetic profiles and intercepted API responses.
   - Check keyboard navigation, conditional fields, validation feedback, save payloads, failure recovery and duplicate submissions.
   - Check 390px and 320px layouts and review desktop and mobile screenshots.
   - Review the page within the running JSP application and complete a save/reload walkthrough with a synthetic account.

## Implementation and evidence

- Items 1–3 are implemented in `backend/src/main/webapp/applicant/application-profile.jsp`, `backend/src/main/webapp/static/js/application-profile.js` and `backend/src/main/webapp/static/css/application-profile.css`.
- Item 4 was implemented in the preceding account and permissions fixes; those changes remain uncommitted in the same worktree.
- Eleven Chromium tests cover the current profile changes. Two existing email-transfer tests and three Java QualificationService tests also pass.
- All 22 original profile input names are retained; dynamic skills and interests keep their original names. Boolean and numeric payload types, leading-zero IDs and synthetic testing metadata are covered by browser tests.
- Browser tests use the actual JSP body, page script and API helper, with JSP directives removed and API responses intercepted. They do not verify JSP compilation, live database persistence, the surrounding application shell, or full assistive-technology compliance.
- The final live JSP application save/reload walkthrough in item 5 is pending. It should not be reported as completed until performed.

## Reproducing checks

Install Playwright as a development tool or point `PLAYWRIGHT_MODULE` to an existing Playwright module. If using an existing Chromium executable, set `CHROMIUM_EXECUTABLE` to its path. With the normal Playwright module and browser installed:

```bash
node --test tests/frontend/application-profile.test.cjs
node --test tests/frontend/auth-email.test.cjs
mvn -f backend/pom.xml -Dtest=QualificationServiceTest test
```

For the live walkthrough, run `./run.sh`, sign in using a synthetic applicant account, open **TA Application Profile**, change a field, save and reload. Verify the changed value persists and a previously submitted application keeps its original snapshot.
