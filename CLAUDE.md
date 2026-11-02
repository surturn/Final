# ColFind

Android hostel finder (Kotlin, Jetpack Compose, Material 3, Firebase). Package `com.sydney.sydney`, Firebase project `my-application-bb250`. See `README.md` for the layout and `docs/RELEASING.md` for the release checklist.

## Working on this machine (Windows)

- Gradle only builds with JDK 17. The default JDK on PATH (26) and Android Studio's bundled JDK (25) both fail. Use `C:\Users\semutryr\.jdks\ms-17.0.19` as `JAVA_HOME`.
- `local.properties` is git-ignored. If it goes missing, recreate it with `sdk.dir=C:/Users/semutryr/AppData/Local/Android/Sdk` (forward slashes).
- Node is installed at `C:\Program Files\nodejs` but is not on PATH in every shell. Add it before running `firebase` or `npm`.
- Rules tests (`firebase-tests`) need JDK 21 or newer for the emulator (`C:\Users\semutryr\.jdks\jbr-21.0.11`). Port 8080 is often busy on this machine, so the tests read the emulator ports from the environment.
- Start the emulator with PowerShell `Start-Process` so it survives the tool call. The AVD is `medium_phone`.

## Design system

Three colour sets split 60 / 30 / 10: canvas, ink and surfaces, emerald accent. Tokens are in `ui/theme/Color.kt`. Reusable interaction components are in `ui/components/Interactive.kt`. Reach for the accent (`colorScheme.tertiary`) sparingly.

## Session state

Last updated 2 November 2026.

**Branches:** `main` holds the merged `feat/release-ready` work. `feat/guest-journey` is 5 commits ahead of it. Nothing from either is pushed yet.

**Done on `feat/guest-journey`:**
- Guest journey: guests browse, search and call without an account. Signed-in users get saved hostels, recently viewed, a profile nudge and the landlord tools. Guests who tap a heart see a sign-in dialog and return to where they were after logging in.
- Restyle with the 60/30/10 palette, Newsreader and Geist fonts, and the interaction kit.
- Fixed photo uploads always failing (`ImageProcessor` bounds-only decode).
- Firestore rules, indexes and Storage rules are deployed to the live project. 27 rules tests pass.

**Blocked:** Firebase Storage needs the Blaze plan (Spark returns HTTP 402), so no photo upload works in production. The owner will upgrade later. Until then hostels cannot be published, so the heart, Saved list, Recently viewed and call button have not been tested with real data.

**Test data in production:** the account `colfind.qa.test@example.com` exists as an approved landlord with a filled profile and no hostels. Delete it in the app (My profile, Delete my account) when testing is finished.

**Next:**
1. Upgrade the Firebase project to Blaze and set a budget alert.
2. Re-run the publish flow. If uploads still return 403, grant Storage's service account the Firebase Rules Firestore Service Agent role, because the Storage rules read `users/{uid}`.
3. Finish the click-through: publish a hostel, heart it as a signed-in user, tap the heart as a guest, open Saved and Recently viewed, call the landlord, sign out, delete the test account and check the console for leftovers.
4. Check the heart colour and motion on a real phone, and check Android 7 fonts.
5. Push `main` and `feat/guest-journey`, then continue the release checklist.

**Open decisions:** none.
