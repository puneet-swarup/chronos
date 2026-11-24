# Chronos — Project State (resume notes)

Last updated: 2026-11-13
Repo: https://github.com/puneet-swarup/chronos (branch main)

This file lets a future Forge Agent session resume exactly where work stopped.
Read this first.

## 1. What this project is
Chronos: a local-first Android automation/scheduling platform (Kotlin, Jetpack
Compose, Room). Two Gradle modules: :chronos-core (pure Kotlin/JVM domain) and
:app (Android UI). MIT licensed.

## 2. Current git state
- Branch: main, working tree clean at last known point.
- Remote: https://github.com/puneet-swarup/chronos.git (origin).
- Commits present: e002580 (initial), ad7b820 (remove temp scripts),
 acf89d3 (theme-agnostic UI + HomeViewModel + donations), 253279b (docs badges/status).
- Last verified remote HEAD == local HEAD == 253279b.

## 3. Done (verified)
- M1 scheduling: Recurrence, RecurrenceEngine, Schedule, ExecutionRouter (tested).
- M2 workflow: Workflow model + WorkflowValidator + IntentCompiler (tested).
- M3 actions: ChronosAction contract, ActionRegistry, 4 built-in actions
 (notification, open_url, launch_app, delay) + DefaultActions.registry().
- M7 AI seam: LocalModelProvider, NoOpModelProvider, SimulatedModelProvider,
 ModelCatalog, RuleBasedParser (English/Hindi/Hinglish), IntentCompiler.
- M4 Android: Compose shell with 6 sections, ChronosTheme (design tokens),
 HomeUiState + HomeViewModel, ServiceLocator (manual DI).
- Room data layer: Entities, DAOs, ChronosDatabase, ScheduleRepository (Room 2.7.1 + KSP).
- Tests: 43 passing (40 core + 3 app HomeViewModelTest). APK builds.
- Docs: README, ARCHITECTURE, PERMISSIONS, TESTING, CONTRIBUTING, SUPPORT,
 CHANGELOG, LICENSE (MIT), .github/FUNDING.yml.
- Donations: GitHub Sponsors (puneet-swarup) + Ko-fi (ko-fi.com/puneetswarup).

## 4. Pending / next work
- M5 Android scheduling adapters: AlarmScheduler (AlarmManager exact),
 WorkScheduler (WorkManager), BootReceiver/ScheduleReconciler re-arm, timezone
 + clock-change receivers. (BootReceiver + ScheduleReconciler stubs exist.)
- M6 integrations: installed-app discovery, device controls (volume/DND/
 flashlight), event triggers, CapabilityManager, AutomationSupervisor.
- M7 real on-device model: LiteRT-LM LocalModelProvider implementation + model
 downloader (Wi-Fi-only, checksum, resumable). SimulatedModelProvider is in place
 for device-free testing.
- UI: wire remaining screens (Schedule/Automations/AI/Activity/Settings) to data;
 make them match the mockups in /images (JPGs - agent cannot view pixels; ask user
 to describe, or implement per PRD six-section layout as now).
- README cosmetic: markdown badges/doc-links were stripped by the transport and
 render as bare URLs - fix by editing README.md in a normal editor.

## 5. Key decisions
- Hilt is NOT usable: its Gradle plugin 2.52 is incompatible with AGP 9.2.0
 ("Android BaseExtension not found"). Using manual DI via ServiceLocator instead.
- Room 2.7.1 (not 2.6.1) required for Kotlin 2.0 + KSP2 compatibility.
- gradle.properties has android.disallowKotlinSourceSets=false (KSP on AGP 9).
- Build toolchain: JDK 17 at C:/Android/jdk17tmp/jdk-17.0.13+11, Android SDK
 C:/Android/Sdk (platform 36), Gradle 9.4.1 wrapper, AGP 9.2.0.
- applicationId com.chronos.app, minSdk 28, compileSdk/targetSdk 36.

## 6. How to resume
1. Read this file, then FORGE_PLAN.md (milestones M0-M8) and README.md.
2. Verify build: gradlew.bat test :app:assembleDebug (set JAVA_HOME first).
3. Continue at milestone 4/5 (pending section above).
4. For git push: fine-grained PAT needs Contents:Read and write. Push via
 git push https://x-access-token:<PAT>@github.com/puneet-swarup/chronos.git main.

## 7. Known environment issue (important)
The Forge Agent tool transport in this session is unreliable: stale/mismatched
results and punctuation stripping on write. See the diagnostic report for fixes.
Verify every important write by re-reading (get_file_info size/lines).
