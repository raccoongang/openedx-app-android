# Landscape Audit Results

Parallel iOS (iPhone 17 / iOS 26.4) + Android (emulator-5554 / Medium_Phone) audit of every screen in `migration_status.md`. Devices locked to **LANDSCAPE** before each screen; scroll reachability + layout + parity vs native repos checked.

Methodology: option **A** — CMP screenshot + native source as tie-breaker when a discrepancy is visible. Native binaries only booted for pixel-parity cases (none this phase).

Legend:
- ✅ pass — layout intact, all interactive elements reachable via scroll/tap, parity with native
- ⚠ cosmetic — functional but has a minor parity/layout deviation
- ❌ broken — content clipped, unreachable button, crash, or logic gap
- ⏭ skipped — trigger-hard / config-disabled / deferred to later phase (reason in Notes)

Screenshots: `audit_screenshots/{ios,android}/<screen>_*.png`

---

## Phase 1 — Auth & bootstrap (2026-04-22)

| Screen | iOS | Android | Scroll | Parity vs native | Notes |
|---|---|---|---|---|---|
| Logistration | ⏭ | ⏭ | — | — | `PRE_LOGIN_EXPERIENCE_ENABLED: false` in all three CMP configs (dev/stage/prod). Native Android dev config matches. Cold start goes straight to SignIn — expected. To exercise this screen, a one-off config override is needed. |
| SignIn | ✅ | ✅ | ✅ both | ✅ | iOS uses default "Open edX" banner; Android uses "Design" theme (expected from theming config). iOS `scroll direction:down` + Android `swipe up fast` both reveal Register / Forgot password / Sign in button. Android keyboard floating-mode does not displace form. iOS keyboard-on-focus not conclusively triggered by Appium tap — retest manually if needed. |
| SignUp (Register) | ⚠ | ✅ | ✅ both | ⚠ iOS subtitle wrong | **iOS bug**: subtitle reads "Create a new account" — should be "Create an account to start learning today!" per `auth/src/commonMain/composeResources/values/strings.xml:28` (key `auth_create_new_account`) which matches both native Android (`auth/src/main/res/values/strings.xml:28`) and native iOS (`Localizable.strings:28 — "SIGN_UP.SUBTITLE"`). Literal "Create a new account" is not in any source — iOS is falling back to some other string. Android CMP renders correctly. Scroll works on both — Password hint + "Show optional fields" + Create account button all reachable. |
| RestorePassword | ⚠ | ✅ | ✅ both | ⚠ iOS title wrong | **iOS bug**: toolbar + H1 show "Forgot your password?" — should be "Forgot password" (no "your", no "?") per `strings.xml:18` key `auth_forgot_your_password` which matches native Android and native iOS `FORGOT.TITLE`. iOS appears to render `auth_forgot_password` (the SignIn link text "Forgot password?") instead. Android CMP renders correctly. Scroll reveals Reset password button on both. |
| WebContent | ⏭ | ⏭ | — | — | Deferred to phase 2. Cleanest entry = Main → Profile → Settings → Privacy Policy (logged-in). No Terms/Privacy hyperlinks visible in SignIn/SignUp copy under current dev config. |
| UpgradeRequired | ⏭ | ⏭ | — | — | Trigger-hard: requires server to return upgrade-required flag. Deferred per audit plan. |
| WhatsNew | ⏭ | ⏭ | — | — | Trigger-hard: requires WhatsNew version bump on logged-in startup. Revisit in phase 2 after re-login. |

**Session findings to fix (iOS only, two string-resolution bugs that probably share a root cause):**
1. `auth / SignUp` subtitle renders "Create a new account" instead of `auth_create_new_account` value.
2. `auth / RestorePassword` title renders "Forgot your password?" instead of `auth_forgot_your_password` value — it looks like the *link* key (`auth_forgot_password`) is being returned instead.

These are consistent with iOS compose-resources returning the wrong key/value. Worth checking how `stringResource(Res.string.auth_*)` resolves in the auth module on iosMain before phase 2 starts — or at least adding to a follow-up list.

**Infrastructure note:** Android CMP app data was cleared at start of phase to reach logged-out state. Before the next logged-in phase, tester must sign in on both platforms (same credentials, whatever was used pre-audit).

---

## Phase 2 — Main & Dashboard (2026-04-22)

Setup: fresh Android + iOS CMP builds rebuilt and reinstalled at the start of this session (previous builds were stale per tester). Signed in on both using the same test account, rotated both back to LANDSCAPE for each screen. iOS `noReset: true` on a fresh install; Android app-data cleared before sign-in.

| Screen | iOS | Android | Scroll | Parity vs native | Notes |
|---|---|---|---|---|---|
| Main (bottom nav host) | ✅ | ✅ | — | ✅ | Learn/Discover/Profile tabs in bottom nav render + switch correctly in landscape on both. Tab icons + labels + selection highlight match. iOS fills above the notch (no system status bar overlay); Android keeps system status bar — expected per platform. |
| LearnFragment (MainScreen.LearnTab) | ✅ | ✅ | ✅ both | ✅ | Thin wrapper — delegates to DashboardGalleryView. No layout of its own, just hosts the gallery. |
| DashboardGalleryFragment | ✅ | ✅ | ✅ both | ✅ | Course card "Dev Course 1 (AXM-1554)" + "2 Past Due Assignments" + "Resume Course → Video Youtube 1.2.3" + "View All Courses (7)" link + 3-col recommendations row (Eddie Test / Videos only / Dates Test / How to become a Clown / Mobile Demo Course / Open edX Demo Course). Scroll reveals recommendations on both. On iOS the count rendered briefly as "(6)" until data refreshed to "(7)" matching Android — cache quirk, not a landscape issue. |
| DashboardListFragment | ⏭ | ⏭ | — | — | Trigger-hard: dev/stage/prod configs all have `Config.dashboardConfig.type = GALLERY`. Requires a one-off config flip to `LIST` to exercise. Deferred. |
| AllEnrolledCoursesFragment | ✅ | ✅ | ✅ both | ✅ | "All Courses" title + search icon + 4 filter chips (All/In Progress/Completed/Expired) + 3-col LazyGrid of all 7 courses. Bottom row (Open edX Demo Course as the 7th) reachable via scroll on both. Chip row fits single line on 874pt + 2400px landscape widths. |
| ProfileFragment | ✅ | ✅ | — | ✅ | Avatar + "Pavlo" + "@Pavlo" handle + "Edit Profile" button + settings gear in top-right. Profile tab highlighted in bottom nav. Content fits landscape without scroll. |
| ManageAccountFragment | ✅ | ✅ | — | ✅ | "Manage Account" title + avatar + name + email (pavlo.netrebchuk@raccoongang.com) + "Edit Profile" outlined button + red "🗑 Delete Account" link at bottom. Fits landscape. |
| SettingsFragment | ✅ | ✅ | ✅ both | ✅ | "Settings" purple-gradient header + Manage Account card + Settings section (Video / Dates & Calendar) + Support section (Contact Support, Version: 1.0.0 ✓ Up-to-date) + red "Log Out" card. Full content reachable via scroll on both. **Testing note:** Appium `appium_gesture swipe` did not drive scroll reliably on Android Compose at higher y values; `UiScrollable.scrollIntoView` + manual taps on fresh element IDs worked. Not an app bug — confirmed scrollable via alternative strategy. |
| VideoSettingsFragment | ✅ | ✅ | — | ✅ | "Video" title + Wi-fi only download toggle (enabled) + "Video streaming quality — Auto" row + "Video download quality — Auto" row. Entire content fits landscape. |
| VideoQualityFragment | ✅ | ✅ | ✅ both | ✅ | Streaming variant + download variant both tested on iOS. Rows: Auto ✓ (Recommended) / 360p (Lower data usage) / 540p / 720p (Best quality). 1080p reachable via scroll on both. Title correctly localized per VM mode (carried forward from CLAUDE.md fix). |
| CalendarFragment (CalendarSettingsView) | ✅ | ✅ | ✅ both | ✅ | "Dates & Calendar" title + "Calendar Sync" section + calendar-with-refresh illustration + bold "Calendar Sync" heading + description paragraph. Enable-sync button reachable via scroll. |
| CoursesToSyncFragment | ⏭ | ⏭ | — | — | Trigger-hard: requires (1) calendar permission granted on iOS (`simctl privacy grant calendar` + Info.plist NSCalendars*UsageDescription), (2) sync enabled, (3) a calendar already created. Fresh install at phase start didn't have any of these. Deferred. |
| DeleteProfileFragment | ✅ | ✅ | ✅ both | ✅ | "Delete account" title + sad-blue-character-in-box illustration + "Are you sure you want to delete your account?" copy (with "delete your account" highlighted in red) + "To confirm this action, please enter your account password." subtitle. Password field + Yes/Cancel buttons below fold — reachable via scroll. |
| EditProfileFragment | ✅ | ✅ | ✅ both | ✅ | "Edit Profile" + "Full profile" subtitle + circular avatar (with edit-icon overlay button) + "Pavlo" + "Switch to limited profile" blue link + "Year of Birth 1998" dropdown. Remaining fields (Country / Language / About me) below fold — reachable via scroll. |
| AnothersProfileFragment | ⏭ | ⏭ | — | — | Trigger-hard: reached via CourseContainer → Discussions → Thread → Comment → tap user avatar/name. Deferred to phase 6 (Discussion stack) where discussion content is already the focus. |

**Phase 2 findings summary**: No landscape-specific bugs found on Main/Dashboard/Profile/Settings/Video/Calendar stacks. Every migrated screen in phase-2 scope either fits in the 402-point (iOS) / 1080-px (Android) landscape viewport or is reachable via scroll. Parity vs native Android/iOS verified visually; layouts + copy + interactive elements line up.

**Carry-forward bugs still open from phase 1** (not re-tested; out of phase-2 scope):
- iOS `SignUp` subtitle renders wrong key.
- iOS `RestorePassword` title renders wrong key.

**Deferred from phase 2 to later phases / session with trigger setup**:
- DashboardListFragment — config flip `GALLERY → LIST`.
- CoursesToSyncFragment — calendar perms + existing sync state.
- AnothersProfileFragment — reached in phase 6 naturally.

**Tester note**: CMP iOS bundle ID is `org.openedx.app.ios` (not `org.openedx.app.cmp` as previously memoised). The memory entry was updated in this session. Native iOS dev build `org.openedx.app.dev` also remains installed on the simulator for tie-breaks but was not needed this phase.

---

## Phase 3 — Discovery (2026-04-23)

Setup: reused phase-2 installed builds (tester confirmed current + logged-in). Dev config `DISCOVERY.TYPE: native` so Discover tab renders `NativeDiscoveryFragment`. Both devices rotated to LANDSCAPE, Discover tab entered via bottom nav.

| Screen | iOS | Android | Scroll | Parity vs native | Notes |
|---|---|---|---|---|---|
| NativeDiscoveryFragment | ✅ (shell) | ✅ (shell) | n/a | ✅ | Toolbar "Discover" + Settings gear (top-right) + full-width search button (`tf_search`) + bottom nav all render correctly in landscape on both. List body returns empty + a "Something went wrong. Please try again later." snackbar on both — this is a dev-backend course-list API failure (not a landscape bug). Snackbar lays out above bottom nav without clipping the Discover tab affordance on both. Zero course cards returned so card-wrap + row scroll on this screen could not be exercised on dev dataset. |
| CourseDetailsFragment | ⏭ | ⏭ | — | — | **Trigger-hard on dev**: only reachable via (a) NativeDiscovery list card tap, (b) CourseSearch result tap, or (c) courseId deep link (which calls `discoveryInteractor.getCourseDetails` — same broken API). Dev backend returns no discoverable courses for the mobile discovery/search endpoints, so no entry path lands on CourseDetails. Defer until dev has at least one discoverable non-enrolled course, or retest on stage where discovery usually populates. |
| CourseSearchFragment | ✅ (shell + empty state) | ✅ (shell + empty state) | n/a | ✅ | NativeDiscovery → search bar → CourseSearch. Back arrow + "Search" title + bordered search TextField + "Search results" H1 + helper "Start typing to find the course" subtitle all render correctly. Typed query "Demo" → iOS reached "Found 0 courses on your request" (API returned empty list); Android showed "Something went wrong" snackbar + spinner (API error — same dev-backend issue). Layout is correct: results header stays above the keyboard on iOS (soft keyboard fills bottom half); on Android, Gboard is in floating-mini mode (does not displace layout). Clear (X) icon inside the search field reachable on iOS; not exercised on Android. Paginated scroll not exercisable on dev dataset. |
| WebViewDiscoveryFragment | ⏭ | ⏭ | — | — | Trigger-hard: `DISCOVERY.TYPE = native` in dev/stage/prod CMP configs, so WebView variant is never entered. Requires a one-off config flip to `webview`. Defer like `DashboardListFragment`. |
| CourseInfoFragment | ⏭ | ⏭ | — | — | Trigger-hard: only reached when `DISCOVERY.TYPE = webview` (via WebViewDiscovery → course-info card or courseId+infoType deep link). Same config-flip gate as WebViewDiscovery. Defer. |
| ProgramFragment | ⏭ | ⏭ | — | — | Trigger-hard: reached via WebViewDiscovery → program card, or CourseInfo → program card, or deep link. All paths gated on `DISCOVERY.TYPE = webview` (or PROGRAM.TYPE webview). Defer. |

**Phase 3 findings summary**: No landscape-specific bugs found on the two screens we could exercise (NativeDiscovery, CourseSearch). Both render their toolbar + search field + error/empty state + bottom nav (or back arrow) correctly in landscape, with no clipping. The dev backend's mobile-discovery course-list + search endpoints are empty/erroring, which blocks a deeper scroll audit (card wrap, pagination, result row) and CourseDetails entirely. This is a data/backend blocker, not a CMP bug.

**Carry-forward bugs still open from phase 1** (not re-tested; out of phase-3 scope):
- iOS `SignUp` subtitle renders wrong key.
- iOS `RestorePassword` title renders wrong key.

**Deferred from phase 3 to later phases / session with working dev discovery dataset**:
- CourseDetailsFragment — needs dev backend to return ≥1 discoverable non-enrolled course, OR retest on stage.
- WebViewDiscoveryFragment — config flip `DISCOVERY.TYPE native → webview`.
- CourseInfoFragment — same config-flip gate.
- ProgramFragment — same config-flip gate.

Screenshots saved: `audit_screenshots/ios/p3_{native_discovery_landscape, course_search_empty, course_search_demo}.png` and Android equivalents.

---

## Phase 4 — Course Container tabs (2026-04-23)

Setup: rebuilt + reinstalled fresh CMP builds on both devices. Signed in on the same test account. Opened **Dev Course 1 (AXM-1554)** and rotated to LANDSCAPE for each tab.

**Root-cause bug found + fixed mid-session**: `foundation/src/commonMain/.../WindowSize.kt` had diverged from the native `openedx-app-foundation-android` library — `isTablet` was simplified to `width != Compact`, which **incorrectly classified phones in landscape as tablets** (phone landscape: width ≈ Medium/Expanded, height = Compact). As a result, `CollapsingLayout` dispatched to `CollapsingLayoutTablet` in landscape and rendered the tall hero + `ExpandedHeaderContent` (org + title, `titleLarge`), leaving ≈150 px for the body — making every tab effectively unusable in landscape.

**Fix**: `WindowSize.isTablet` + `windowSizeValue` updated to require BOTH `width != Compact && height != Compact` (parity with the native Android library — see `openedx-app-foundation-android/foundation/src/main/java/org/openedx/foundation/presentation/WindowSize.kt:19-29`). After rebuild, `CollapsingLayoutMobile` landscape branch runs → `CollapsedHeaderContent` renders (single-line title only), body gets the full remaining viewport, all tab content becomes scrollable + usable. No behavioural change on phone portrait or on tablet (both orientations).

Pre-fix and post-fix screenshots saved alongside each other in `audit_screenshots/{ios,android}/p4_*_landscape{,_fixed}.png`.

| Screen | iOS | Android | Scroll | Parity vs native | Notes |
|---|---|---|---|---|---|
| CourseContainerScreen (host) | ✅ (post-fix) | ✅ (post-fix) | ✅ (tab row horizontally scrollable) | ✅ | Tab row: Home / Content / Progress / Dates / Downloads / Discussions / More — all 7 tabs tap + switch correctly. Collapsed header shows back arrow + single-line "Dev Course 1 (AXM-1554)". iOS tab row wider than 874pt viewport so Discussions/More clip until scrolled — this also happens on native Android in narrow landscapes; both platforms scroll the row. |
| CourseHomeScreen | ✅ | ✅ | ✅ both | ✅ | Resume bar "Text 1.2.2 Continue →" + `CourseHomePager` cards: "Course Completion 55% Completed" visible. Videos / Assignments / Grades pages reachable via swipe of the pager + HomeNavigationRow. |
| ContentTabScreen (All / Videos / Assignments) | ✅ | ✅ | ✅ both | ✅ | Sub-tab pill row renders full-width; "0/1 Sections Completed" progress header + resume bar + `Section 1` expandable section — expanded shows `Subsection 1.2 · Final Final · Due Jan 08, 2025 · 0/1` + `Subsection 1.3` rows. All three sub-tabs (All / Videos / Assignments) swap correctly. |
| CourseDatesScreen | ✅ | ✅ | ✅ both | ✅ | PLS banner fully visible: "Missed some deadlines? Don't worry - shift our suggested schedule to complete past due assignments without losing any progress." + full-width "Shift due dates" primary button. "Offline" row + "Completed · 1 Item Hidden" expandable section below. |
| DiscussionTopicsScreen (Discussions tab) | ✅ | ✅ | ✅ both | ✅ | "Search all posts" bar + "Main categories" heading + two cards (All Posts / Posts I'm following) + "General" topic row all visible. |
| HandoutsScreen (More tab) | ✅ | ✅ | — | ✅ | Two rows: Handouts / "Find important course information" + Announcements / "Keep up with the latest news". Both clickable. |
| HandoutsWebViewScreen | ✅ | ✅ | — | ✅ | Empty-state: "There are currently no handouts for this course." centered below the illustration. Title "Handouts" + back arrow render correctly at the top. |
| CourseProgressScreen | ✅ | ✅ | ✅ both | ✅ | Course Completion section (55% ring) + Overall Grade ("Current Overall Weighted Grade: 0%", 50% target marker on the bar) + amber warning pill: "A weighted grade of 50% is required to pass this course". iOS previously showed `50%%` (double-percent) because compose-resources on iOS does not honour the `%%` → `%` escape in Android-style `strings.xml`; fix landed in this session (see **%% escape fix** below). |
| CourseOfflineScreen (Downloads tab) | ✅ | ✅ | — | ✅ | "0MB · Available to download" + "None of this course's content is currently available to download offline." + full-width "Download all" button. Tab is labelled **"Downloads"** (from `course_container_nav_downloads`) even though the code enum + migration list call it `OFFLINE` — parity with native Android. |
| CourseSectionFragment | ✅ | ✅ | — | ✅ | "Subsection 1.2" title + back arrow + unit rows: Single 1.2.1, Text 1.2.2, Video Youtube 1.2.3 (shown completed with blue check). |
| CourseUnitContainerFragment (shell) | ✅ (post-fix) | ✅ | — | ✅ | Shell renders on both platforms: "Raw HTML" title, back arrow, HtmlUnit body ("Demo text This is a Raw HTML editor that saves your HTML exactly as you enter it…"), "Finish" button at bottom-left. **iOS was initially crashing to springboard (SIGABRT / unhandled-Kotlin-exception through MetalRedrawer.draw) on any attempt to open an HtmlUnit** — root cause `JsInjectionProvider` was registered only in Android's `app/.../di/AppModule.kt` (not in `commonScreenModule`), so `HtmlUnitViewModel(...get()...)` failed Koin resolution on iOS. Fix: moved `factory<JsInjectionProvider> { JsInjectionProviderImpl() }` into `shared/.../di/CommonScreenModule.kt` and removed the Android duplicate (see **Koin fix** below). |

**Phase 4 findings summary** (three bugs found + all fixed this session):

1. **`WindowSize.isTablet` regression** (landscape layout): `foundation/.../WindowSize.kt` had `isTablet = width != Compact` instead of the native-library `height != Compact && width != Compact`. Phones in landscape classified as tablets → `CollapsingLayout` tablet branch → tall hero + ExpandedHeaderContent → ~150 px body on every tab. Fix landed in `foundation/src/commonMain/.../WindowSize.kt:11-22`.

2. **iOS `JsInjectionProvider` missing in Koin** (iOS HtmlUnit crash): `factory<JsInjectionProvider> { JsInjectionProviderImpl() }` was only in Android `AppModule.kt`, so on iOS `HtmlUnitViewModel` resolution threw `NoDefinitionFound` during Compose composition → MetalRedrawer raised an unhandled Kotlin exception → SIGABRT (three diagnostic reports captured in `~/Library/Logs/DiagnosticReports/iosApp-2026-04-23-*.ips`). Fix: moved the factory into `shared/.../di/CommonScreenModule.kt` (removed Android duplicate from `AppModule.kt`). Same "MetalRedrawer stack" pattern recorded in existing memory `feedback_kmp_ios_koin_modules.md`.

3. **`%%` escape not honoured on iOS compose-resources** (copy bug): three strings in `course/.../composeResources/values/strings.xml` used Android-style `%%` → `%` escape (`course_progress_required_grade_percent`, `course_progress_current_and_max_weighted_graded_percent`, `course_of_grade`). Android renders `%`; iOS renders literal `%%`. Fix: removed `%%` from the three strings and pass `"N%"` explicitly from the Kotlin call sites (`CourseProgressScreen.kt`, `GradesHomePagerCardContent.kt`, `CourseContentAssignmentScreen.kt`). Verified iOS now shows "A weighted grade of 50% is required to pass this course".

Every screen in phase-4 scope now renders correctly in landscape on both iOS and Android; parity verified.

**Additional sanity checks performed at end of session**:
- **Portrait regression**: rotated both phones to portrait after all fixes were applied. Android Learn-tab gallery renders normally (expanded hero + course cards + bottom nav). iOS in-course Progress tab also renders correctly (expanded hero + testOrg1 + Dev Course 1 title + tab row + full Progress body with the `%%` fix visible — "A weighted grade of 50% is required to pass this course" + Grade Details rows "0 / 50%"). No regressions from the `WindowSize.isTablet` or `%%` fixes. Screenshots: `p4_portrait_regression_check.png` on both platforms.
- **iOS tab row reachability**: on iPhone 17 (874pt landscape), only Home→Discussions fit; "More" is off-screen right by default. Verified the LazyRow scrolls horizontally via a W3C pointer drag (the built-in `swipe`/`scroll` gestures on top of the buttons get absorbed by the button tap handler — test harness quirk, not an app bug). Post-scroll, "More" becomes visible and tappable. All 7 tabs reachable. Saved: `p4_ios_tab_row_scrolled_more.png`.
- **CourseUnitContainer shell details**: tapping the unit title row does not reveal a dropdown in the dev config — `isCourseExpandableSectionsEnabled` is off for this course. Next/Prev buttons render inside the Scaffold but are gated on the expandable-sections config and did not appear. Android behaves identically. Not a landscape bug; parity confirmed.

**Carry-forward bugs still open from phase 1** (not re-tested; out of phase-4 scope):
- iOS `SignUp` subtitle renders wrong key.
- iOS `RestorePassword` title renders wrong key.

**New carry-forward items into phase 5** (Unit types + media): none from phase 4. The iOS HtmlUnit crash that was initially tagged for phase 5 was root-caused and fixed in this session (`JsInjectionProvider` Koin registration); HtmlUnit now opens + renders identically to Android.

Screenshots saved: `audit_screenshots/{ios,android}/p4_*_landscape_fixed.png` (post-fix) plus the pre-fix baselines `p4_course_container_home_landscape.png`, `p4_content_*_landscape.png`, `p4_dates_landscape.png`, `p4_discussions_topics_landscape.png`, `p4_more_handouts_landscape.png` which illustrate the cramped-body symptom caused by the `WindowSize.isTablet` regression. New: `audit_screenshots/ios/p4_course_unit_container_html_landscape_fixed.png` (iOS HtmlUnit post-Koin-fix) and `audit_screenshots/ios/p4_progress_landscape_percent_fixed.png` (iOS Progress tab post-`%%` fix).

---

## Phase 5 — Unit types + media (pending)

_Trigger next session: "Продовжуй landscape audit фазу 5"_
