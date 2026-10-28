<%@ page pageEncoding="UTF-8" %>
<%@ include file="/common/auth-check.jsp" %>
<jsp:include page="/common/layout-top.jsp"/>
<link rel="stylesheet" href="<%= request.getContextPath() %>/static/css/application-profile.css">
<div class="application-profile">
  <header class="profile-intro">
    <p class="profile-eyebrow">Student application</p>
    <h1>TA Application Profile</h1>
    <p>Tell course reviewers about your background, qualifications and interests.</p>
    <p class="profile-note">Saving this profile does not submit a job application. Previously submitted applications keep their original information.</p>
  </header>
  <div class="profile-actions"><button id="qualificationEdit" aria-controls="qualificationForm" type="button" class="btn btn-outline" hidden>Edit application profile</button></div>
  <nav class="profile-nav" aria-label="Application profile sections">
    <a href="#profile-identity">1. Identity &amp; academics</a>
    <a href="#profile-experience">2. Experience &amp; training</a>
    <a href="#profile-interests">3. Skills &amp; interests</a>
    <a href="#profile-arrangements">4. Schedule &amp; arrangements</a>
    <a href="#profile-confirmation">5. Confirm &amp; save</a>
  </nav>
  <div class="profile-feedback">
    <p id="qualificationMessage" role="status" aria-live="polite" aria-atomic="true">Loading your application profile…</p>
    <button id="qualificationRetry" type="button" class="btn btn-outline" hidden>Retry loading profile</button>
  </div>
  <div id="qualificationView" aria-label="Saved application profile" hidden></div>
  <div id="qualificationErrors" class="profile-errors" role="alert" tabindex="-1" hidden>
    <h2>Please review the following fields</h2><ul id="qualificationErrorList"></ul>
  </div>
  <noscript><p>JavaScript is required to load and save your application profile.</p></noscript>
  <form id="qualificationForm" aria-busy="true" hidden>
    <p class="profile-required-note">Fields labelled <strong>Required</strong> must be completed. Other fields are optional.</p>
    <fieldset id="qualificationFields" disabled>
      <legend class="profile-sr-only">TA application profile details</legend>
      <section class="profile-section" aria-labelledby="profile-identity">
        <h2 id="profile-identity" tabindex="-1"><span>01</span> Identity and academic information</h2>
        <p class="profile-section-description">Use your UCI details so reviewers can identify your application.</p>
        <div class="profile-grid">
          <label for="q-firstName">First name <span class="profile-required">Required</span><input id="q-firstName" name="firstName" autocomplete="given-name" class="input" required maxlength="100"></label>
          <label for="q-lastName">Last name <span class="profile-required">Required</span><input id="q-lastName" name="lastName" autocomplete="family-name" class="input" required maxlength="100"></label>
          <label for="q-uciEmail">UCI email <span class="profile-required">Required</span><input id="q-uciEmail" name="uciEmail" type="email" autocomplete="email" class="input" required maxlength="254" aria-describedby="uciEmailHint"><small id="uciEmailHint">Use your @uci.edu or UCI subdomain email address.</small></label>
          <label for="q-uciStudentId">UCI student ID <span class="profile-required">Required</span><input id="q-uciStudentId" name="uciStudentId" class="input" required pattern="[0-9]{8}" maxlength="8" inputmode="numeric" aria-describedby="studentIdHint"><small id="studentIdHint">Enter exactly 8 digits, including any leading zeros.</small></label>
          <label for="q-studentLevel">Student level <span class="profile-required">Required</span><select id="q-studentLevel" name="studentLevel" class="input" required><option value="undergraduate">Undergraduate</option><option value="masters">Master's</option><option value="phd">PhD</option></select></label>
          <label for="q-program">Academic program <span class="profile-required">Required</span><input id="q-program" name="program" class="input" required maxlength="200"></label>
          <label for="q-graduateStartYear">Graduate program start year <span id="graduateYearRequirement" class="profile-required">Required for graduate students</span><input id="q-graduateStartYear" name="graduateStartYear" type="number" min="1900" max="<%= java.time.Year.now().getValue() %>" step="1" class="input" aria-describedby="graduateYearHint"><small id="graduateYearHint">Required for master's and PhD students; optional for undergraduates.</small></label>
          <label for="q-facultyPI">Faculty / PI <span class="profile-required">Required</span><input id="q-facultyPI" name="facultyPI" class="input" required maxlength="200" aria-describedby="facultyHint"><small id="facultyHint">Enter N/A if this does not apply to you.</small></label>
        </div>
      </section>
      <section class="profile-section" aria-labelledby="profile-experience">
        <h2 id="profile-experience" tabindex="-1"><span>02</span> Experience and training</h2>
        <p class="profile-section-description">Report your current experience and training status.</p>
        <div class="profile-grid">
          <label for="q-taQuarters">Previous TA quarters at UCI <span class="profile-required">Required</span><input id="q-taQuarters" name="taQuarters" type="number" min="0" max="100" step="1" required value="0" class="input" aria-describedby="quartersHint"><small id="quartersHint">Exclude summer quarters. Enter 0 if you have not served as a TA.</small></label>
          <label for="q-advancedToCandidacy">Have you advanced to candidacy? <span class="profile-required">Required</span><select id="q-advancedToCandidacy" name="advancedToCandidacy" class="input" required><option value="">Choose</option><option value="yes">Yes</option><option value="no">No</option><option value="not_applicable">N/A</option></select></label>
          <label for="q-trainingStatus">TA Training status <span class="profile-required">Required</span><select id="q-trainingStatus" name="trainingStatus" class="input" required><option value="">Choose</option><option value="completed">Completed</option><option value="not_started">Not started</option><option value="in_progress">In progress</option><option value="not_applicable">N/A</option></select></label>
          <label for="q-sexualHarassmentTrainingCompleted">Completed Online Sexual Harassment Training? <span class="profile-required">Required</span><select id="q-sexualHarassmentTrainingCompleted" name="sexualHarassmentTrainingCompleted" class="input" required><option value="">Choose</option><option value="true">Yes</option><option value="false">No</option></select></label>
        </div>
        <label for="q-teachingExperience">Teaching / experience details <span class="profile-optional">Optional</span><textarea id="q-teachingExperience" name="teachingExperience" class="input" rows="3" maxlength="4000"></textarea></label>
        <label for="q-researchExperience">Research experience <span class="profile-optional">Optional</span><textarea id="q-researchExperience" name="researchExperience" class="input" rows="3" maxlength="4000"></textarea></label>
      </section>
      <section class="profile-section" aria-labelledby="profile-interests">
        <h2 id="profile-interests" tabindex="-1"><span>03</span> Skills and course interests</h2>
        <p class="profile-section-description">Choose the skills and courses relevant to you. Available options are configured by course staff.</p>
        <fieldset class="profile-choices"><legend>Skills <span class="profile-optional">Optional</span></legend><div id="qualificationSkills" class="profile-choice-grid"></div></fieldset>
        <fieldset class="profile-choices"><legend>Courses of interest <span class="profile-optional">Optional</span></legend><div id="qualificationInterests" class="profile-choice-grid"></div></fieldset>
      </section>
      <section class="profile-section" aria-labelledby="profile-arrangements">
        <h2 id="profile-arrangements" tabindex="-1"><span>04</span> Schedule and assignment arrangements</h2>
        <label for="q-additionalEmployment">Additional on-campus employment this quarter <span class="profile-required">Required</span><textarea id="q-additionalEmployment" name="additionalEmployment" class="input" rows="3" maxlength="1000" required aria-describedby="employmentHint"></textarea><small id="employmentHint">For example, Housing Assistant. Enter None if you have no other on-campus employment.</small></label>
        <label for="q-documentedAccommodation">Do you have a documented accommodation filed with the UCI Disability Services Center that needs consideration for ASE assignments? <span class="profile-required">Required</span><select id="q-documentedAccommodation" name="documentedAccommodation" class="input" required><option value="">Choose</option><option value="true">Yes</option><option value="false">No</option></select></label>
        <label for="q-accommodationNotes">Accommodation arrangement notes <span class="profile-optional">Optional</span><textarea id="q-accommodationNotes" name="accommodationNotes" class="input" rows="3" maxlength="2000" aria-describedby="accommodationHint"></textarea><small id="accommodationHint">Describe assignment needs only.</small></label>
        <label for="q-schedulingConflicts">Scheduling conflicts / notes <span class="profile-optional">Optional</span><textarea id="q-schedulingConflicts" name="schedulingConflicts" class="input" rows="3" maxlength="4000" aria-describedby="scheduleHint"></textarea></label>
        <p id="scheduleHint" class="profile-note">Also complete <a href="<%= request.getContextPath() %>/applicant/availability.jsp">Weekly Availability</a> for each application cycle. These notes do not replace your weekly schedule.</p>
        <label for="q-additionalComments">Additional comments <span class="profile-optional">Optional</span><textarea id="q-additionalComments" name="additionalComments" class="input" rows="3" maxlength="4000"></textarea></label>
      </section>
      <section class="profile-section" aria-labelledby="profile-confirmation">
        <h2 id="profile-confirmation" tabindex="-1"><span>05</span> Confirm and save</h2>
        <p class="profile-section-description">All three acknowledgements are required.</p>
        <label class="profile-check" for="q-ackSchedule"><input id="q-ackSchedule" name="ackSchedule" type="checkbox" required><span>By checking this box, I acknowledge that I have consulted the Schedule of Classes to minimize scheduling conflicts with my potential ASE assignment.</span></label>
        <label class="profile-check" for="q-ackEnrollment"><input id="q-ackEnrollment" name="ackEnrollment" type="checkbox" required><span>By checking this box, I acknowledge that I need to enroll as a full time student (at least 12 units) and pay any outstanding balance on my Zot account prior to the fee payment deadline before my tuition remission can be applied.</span></label>
        <label class="profile-check" for="q-acknowledged"><input id="q-acknowledged" name="acknowledged" type="checkbox" required><span>I confirm that the information I submit is accurate to the best of my knowledge.</span></label>
        <div class="profile-save"><button id="qualificationCancel" type="button" class="btn btn-outline" hidden>Cancel editing</button><button id="qualificationSave" type="submit" class="btn btn-primary">Save application profile</button><p id="qualificationSaveHint">Save your profile, then apply to a position from Find Jobs.</p></div>
      </section>
    </fieldset>
  </form>
</div>
<jsp:include page="/common/layout-bottom.jsp"/>
<script src="<%= request.getContextPath() %>/static/js/application-profile.js"></script>
