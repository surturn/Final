# Releasing ColFind

## Signing

1. Create an upload key once and keep it out of git:
   ```
   keytool -genkeypair -v -keystore colfind-upload.jks -alias colfind -keyalg RSA -keysize 4096 -validity 10000
   ```
2. Create `keystore.properties` in the repository root (git ignores it):
   ```
   storeFile=/absolute/path/colfind-upload.jks
   storePassword=...
   keyAlias=colfind
   keyPassword=...
   ```
   In CI the same values come from `SIGNING_STORE_FILE`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS` and `SIGNING_KEY_PASSWORD`.
3. Enrol in **Play App Signing** when you create the app in Play Console. Back up the upload keystore and its passwords somewhere other than your laptop.
4. Build the bundle: `./gradlew bundleRelease` and upload `app/build/outputs/bundle/release/app-release.aab`.

Raise `versionCode` in `app/build.gradle.kts` for every upload.

## Deploy checklist

### Before the build
- [ ] CI is green on the release branch (lint, unit tests, debug and release builds, rules tests)
- [ ] The Firebase project is on the **Blaze** plan. Cloud Storage no longer works on the free Spark plan (uploads return HTTP 402), so hostel and profile photos fail until it is upgraded. Set a budget alert at the same time
- [ ] `targetSdk` meets the current Play requirement (API 36 or higher for new apps and updates since 31 August 2026)
- [ ] Firestore and Storage rules are deployed from this commit (`firebase deploy --only firestore:rules,firestore:indexes,storage`)
- [ ] At least one admin document exists in `admins/`
- [ ] API key is restricted to the app package and signing SHA-1 values
- [ ] Budget alert is set on the Firebase project
- [ ] `versionCode` raised, `versionName` updated, `CHANGELOG.md` updated

### Smoke test on a real phone with the signed release build
- [ ] Register a student, close the app, reopen: still signed in
- [ ] Log out: private screens are not reachable, hostels still browse
- [ ] Log in with a wrong password: clear error message
- [ ] Edit profile with a camera photo: photo appears, phone number is not visible in the directory
- [ ] Apply as landlord, approve in the console, return to the app: **Add a hostel** appears
- [ ] Publish a hostel with a photo, open it, tap **Call the landlord**
- [ ] A normal student cannot reach **Add a hostel** content
- [ ] Delete a test account: profile, photo and its hostels are gone from the console
- [ ] Airplane mode: lists show a retry message instead of crashing

### Play Console
- [ ] Privacy policy URL (the app collects name, email, phone, photos and a user id)
- [ ] Data safety form matches the data above, with deletion in the app. Also provide a web page or email for deletion requests, which Play asks for
- [ ] Content rating questionnaire, target audience and contacts filled in
- [ ] Store listing text, icon (512 px), feature graphic and at least two phone screenshots
- [ ] Release to internal testing first, then a closed test (new personal developer accounts must run a closed test with testers for 14 days before production access; check the current rule in Play Console)
- [ ] Production rollout as a staged release starting at 5 percent

### Rollback triggers
Halt the staged rollout and fix forward if any of these happen in the first days:
- Crash rate or ANR rate in Android vitals climbs towards Play's bad behaviour thresholds
- Sign in, hostel listing or photo upload fails for more than a handful of users
- Firebase usage or cost spikes unexpectedly

A release cannot be pulled back from phones that already have it, so the real rollback is a fixed build with a higher `versionCode`. Keep the previous signed bundle for comparison. Rules changes can be rolled back from the Firebase console rules history.

### After release
- [ ] Watch Android vitals and the Firebase console for the first 48 hours
- [ ] Tag the release in git (`git tag v1.0.0`)
- [ ] Answer early reviews

## Known gaps to plan for
- **App Check** is not enabled. It makes scraping and abuse of your Firebase project harder. Add Play Integrity App Check and enforce it once the app is live.
- Email addresses are not verified. Add email verification if you want to stop fake accounts.
- Landlord approval is manual in the console. An admin screen or Cloud Function is the next step if requests grow.
- Crash reporting (for example Firebase Crashlytics) is not wired in.
