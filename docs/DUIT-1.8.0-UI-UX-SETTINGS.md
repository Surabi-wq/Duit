# Duit 1.8.0 — UI/UX and Settings Upgrade

## Accepted product direction
- Version: 1.8.0 (versionCode 18)
- Visual direction: hybrid premium fintech
- Palette: navy blue and gold
- Navigation: adaptive intent, compact primary destinations
- Theme default: follow Android system; manual light/dark override
- Density: compact
- Dashboard direction: executive
- Transaction entry: hybrid quick/full flow (existing transaction flow retained)
- Charts: professional
- Settings: searchable categorized hub, enterprise-oriented information architecture
- Quick actions: transaction, transfer/account, receipt, recurring, bill
- Motion: subtle
- Onboarding: guided
- Launcher identity: refreshed navy/gold vector

## Implemented in this change set
1. Added DuitSettingsScreen.kt as a searchable 13-category settings hub.
2. Added Settings as a primary navigation destination.
3. Added live theme preference (system, light, dark) persisted in SharedPreferences.
4. Replaced the prior green-centric Material palette with navy/gold light and dark color schemes.
5. Added compact density preference, language preference storage, notification sound/vibration preference storage, guided-onboarding preference, and balance visibility control.
6. Refreshed the launcher vector identity.
7. Set Android version to 1.8.0 and release artifact naming to Duit-1.8.0.

## Important implementation boundaries
The settings hub distinguishes available behavior from informational placeholders. Language selection currently stores a preference but does not translate every screen. Sound/vibration preferences are stored but do not yet control a complete notification delivery pipeline. Cloud/device synchronization is not configured. The balance-visibility switch is an interface privacy feature, not app lock or database encryption. Backup/restore remains in the existing Help/Guide flow. The compact density preference is stored; full density adaptation across every legacy screen remains a follow-up task.

## Validation
GitHub Actions is configured to run unit tests and assemble the Android Release APK. The CI result must be checked before describing the build as successful. A successful assembleRelease does not replace physical-device UI testing, accessibility review, or Room migration instrumentation tests.
