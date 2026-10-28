# Role access fixes implementation plan

Goal: Fix audit findings 1–5 on XinyiXu based on d8f1835; retain the earlier Interview removal and frontend changes. No commit or push until the user confirms the final result.

- [x] Add regression tests reproducing unrelated profile access, revoked teacher grants, reassigned-course access, inconsistent rejection and generic account-editor bypasses.
- [x] Restrict message user-info by active account, permitted relationship/current course and necessary fields; scope student application summaries to the requesting teacher.
- [x] Unify teacher application reads/actions with current course ownership and ReviewAccess. Route final rejection through ReviewService so approval permission and decision audit are consistent.
- [x] Define course reassignment as transfer of current course and posting ownership, preserving historical actor/snapshot data; apply the policy to normal and generic module updates. Ensure previous teacher loses access and new teacher can configure/review/assign.
- [x] Disable generic Data Editor account mutations in favor of Users business endpoints, including validation and self-account protection; retain only safe account projections.
- [x] Run targeted and broader regression tests, review changes and maintain docs/XinyiXu-change-log.md including Interview removal, fixes, verification and known limits.
- [ ] Present branch, diff, test results and changelog for approval. Do not commit before explicit confirmation.
