# Compose Multiplatform Migration

## Status: ~95% Complete (59 commits)

Branch: `feature/compose-multiplatform-migration`

### What's Done

#### Build System (Phase 0) ✅
- All 13 build.gradle files converted to Kotlin DSL
- Version catalog (`gradle/libs.versions.toml`)
- `ConfigHelper.groovy` → `ConfigHelper.kt`

#### Foundation Module (Phase 1) ✅
- External `openedx-app-foundation-android:1.1.1` replaced with in-project `foundation` module
- 42 classes ported: BaseViewModel, ResourceManager, UIMessage, WindowSize, FileUtil, etc.

#### Source Split (Phase 2) ✅
- **409 files (63.5%)** in `commonMain` across all 11 modules
- **40 ViewModels** moved to commonMain
- **Theme** (AppColors, AppShapes, AppTypography) in commonMain

#### Networking (Phase 3) ✅
- **Gson: 0 files** (was 77) — 100% migrated to kotlinx.serialization
- **Retrofit: 0 files** (was 7) — 100% migrated to Ktor
- All 7 API classes use Ktor HttpClient
- NetworkingModule uses Ktor with OkHttp engine (Android)
- Custom serializers (CourseEnrollments) use kotlinx KSerializer

#### Navigation Infrastructure (Phase 5) 🔶 Partial
- Router interfaces decoupled from FragmentManager (use `Any?`)
- FragmentManager removed from all ViewModels (40+)
- Navigation routes defined (`AppNavRoutes`, `AuthRoutes`)
- AppNavigator bridge for NavController
- AppActivity converted to Compose `setContent`
- **62 Fragments remain** — need conversion to Compose Navigation destinations

#### KMP Module (Phase 6) ✅
- `shared` module with `kotlin-multiplatform` plugin
- Targets: androidTarget, iosX64, iosArm64, iosSimulatorArm64
- Compose Multiplatform 1.8.0

#### Platform Abstractions (Phase 7) 🔶 Partial
Implemented expect/actual:
- `Platform` (name)
- `NetworkConnection` (ConnectivityManager / NWPathMonitor)
- `SecureStorage` (EncryptedSharedPreferences / NSUserDefaults)
- `ConfigLoader` (assets / NSBundle)
- `PlatformWebView` (WebView / WKWebView)
- `PlatformVideoPlayer` (ExoPlayer / AVPlayer)

Common interfaces:
- `AnalyticsService`
- `DeepLinkHandler`
- `PushNotificationService`
- `CalendarService`

#### iOS App (Phase 8) ✅
- Xcode project (via xcodegen)
- SwiftUI entry point with ComposeUIViewController
- Koin DI initialization
- **BUILD SUCCEEDS** on iOS Simulator

#### Other
- Parcelable removed from 19 domain models (13 remain for Bundle passing)
- kotlin-parcelize removed from 4 modules
- viewBinding removed from 6 modules
- XML layout removed from AppActivity

### What Remains

#### Fragment Removal (~20% of total work)
62 Fragments need conversion to Compose Navigation destinations.
Key challenges:
- `MainFragment` uses ViewPager2 + BottomNavigationView → needs Scaffold + NavigationBar
- `LearnFragment` has nested ViewPager2
- `CourseContainerFragment` has tabbed pager
- DialogFragments need Compose Dialog/BottomSheet equivalents
- Fragment result communication needs replacement
- Deep link routing needs NavController adaptation

#### Resources Migration
- strings.xml → composeResources (CMP resource system)
- drawables → composeResources
- Affects 100+ files with `R.string.*` / `R.drawable.*` references

#### Remaining Platform Implementations
- Calendar actual implementations
- Push notification actual implementations
- Analytics actual implementations
- Branch SDK deep link actual implementations

### Key Architecture Decisions
1. **Ktor + OkHttp engine** for Android networking (keeps OkHttp interceptors working)
2. **kotlinx.serialization** everywhere (zero Gson)
3. **Router interfaces use `Any?`** instead of FragmentManager for KMP compatibility
4. **AppActivity uses Compose `setContent`** with embedded FragmentContainerView
5. **Per-module commonMain** via sourceSets configuration (not actual KMP plugin yet for feature modules)
6. **shared module** uses actual kotlin-multiplatform plugin with iOS targets

### Build Commands
```bash
# Android
./gradlew assembleDevelopDebug
./gradlew testDevelopDebugUnitTest

# iOS Framework
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# iOS App (from iosApp/)
xcodebuild -project iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 17' build
```
