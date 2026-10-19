# Android multi-instance: device evidence

Screenshots for the Android multi-instance stack (port of openedx-app-ios #672–#685).
This branch holds evidence only; it is not meant to be merged.

## Setup

- Backend: local Open edX "Verawood-dev" (edx-platform `0f57cf2bbf13358af5343f0fc2ddd952b656d570`,
  Tutor 22.0.0, image `openedx-dev:verawood-0f57cf2b`), one LMS reachable as two sites:
  - **Verawood North**: `http://localhost:19000`, OAuth client `mi-site-a`, learner `learner_a`.
    Matches the build's app-level `API_HOST_URL` / `OAUTH_CLIENT_ID`.
  - **Verawood South Campus**: `http://localhost:19001` (same LMS through a second
    `adb reverse` port), OAuth client `mi-site-b`, learner `learner_b`, pink branding.
  Both learners are enrolled in the same course, so the two sites share course and block IDs.
- App: `prodDebug` from the top of the stack, with a test-only `config.yaml` listing the two sites;
  a debug manifest override allowed cleartext HTTP to the local LMS (not part of the PRs).
- Device: Android emulator, Android 16 (API 36), `sdk_gphone64_arm64`.

## What the screenshots show

| # | Check |
|---|---|
| 01 | Multi-site build opens on site selection. |
| 02 | Choosing South opens its sign-in, branded, with the selected site and a Change link. |
| 03 | South signed in; requests went to `localhost:19001` with client `mi-site-b` (LMS log). |
| 04–05 | Settings shows the current site card with Log Out; it opens Learning Sites. |
| 06 | North signed in; requests to `localhost:19000` with `mi-site-a`. |
| 07 | Both sites keep their sessions. |
| 08 | Back to South with no sign-in and no token request. |
| 09–10 | Logging out of North (not selected) keeps South signed in. |
| 11 | Logging out of the selected site returns to site selection with the default theme. |
| 12 | Learning Sites in dark mode. |
| 13–14 | A single-site build (no `INSTANCES`) looks as before: no picker, no site card. |
| 15–16 | Upgrade: signed in on a `develop` build, then the stack installed over it lands on the dashboard of the matching site without signing in again. |
