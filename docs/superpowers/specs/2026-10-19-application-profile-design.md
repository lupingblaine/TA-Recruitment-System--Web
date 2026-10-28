# Student application profile design

The user approved the previously proposed student profile improvements on October 19, 2026. Improve the existing JSP and JavaScript in place; retain field names, API URLs, acknowledgement text and submitted snapshot semantics.

Use five visible sections: identity and academic information; experience and training; skills and course interests; scheduling and arrangements; acknowledgements. Provide keyboard-accessible anchor navigation, labelled controls, required-field guidance, graduate-year conditional guidance, and a validation summary linking to invalid fields. Do not introduce additional eligibility rules.

Disable editing during initial load and save. Announce loading, unsaved edits, saving, success and errors. A failed load offers retry; failed saves retain user input. Render API strings as text. Saving the profile is distinct from submitting a job application. Use a scoped responsive stylesheet with visible focus indicators.

Verify the real JSP body and JavaScript in Chromium with intercepted synthetic API responses: section navigation, conditional requirements, validation, serialization, save failure recovery, load retry, empty options and mobile overflow. This browser fixture does not validate server authentication or JSP compilation. Verify Java validation separately. Keep all previous uncommitted changes; do not create a branch or commit without a request.

October 20 follow-up: saved profiles open as text, following the existing Profile page pattern. Edit opens the populated form; a successful save displays the returned saved details, while Cancel restores the latest saved values without a request. Failed saves retain the editing form. First-time profiles open for entry. Section navigation works in either mode.
