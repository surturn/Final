# ColFind

ColFind is an Android app that helps students in Kenya find a hostel near their campus and find other students. Landlords who are approved by an admin can list hostels with a photo, a price and a contact number.

Built with Kotlin, Jetpack Compose (Material 3) and Firebase (Auth, Firestore, Storage).

## What the app does

- Anyone can browse and search hostels and call the landlord.
- Students create an account, keep a profile (name, campus, area, photo, private phone number) and appear in a student directory that only signed-in users can see. Phone numbers and emails are never shown to other students.
- Anyone can apply to become a landlord. An admin approves the request in the Firebase console. Only approved landlords can publish hostels.
- Users can delete their account and all their data from the profile screen.

## Project layout

```
app/src/main/java/com/sydney/sydney/
  data/model      plain data classes (UserProfile, Hostel, StudentEntry)
  data/repo       repository interfaces and the user-facing error type
  data/firebase   Firebase implementations, document mapping, photo compression
  domain          validators, search filters, auth error messages (pure Kotlin)
  ui/*            one package per feature: ViewModel plus Compose screen
  navigation      routes and the NavHost, including sign-in gating
firestore.rules, storage.rules   security rules (the real access control)
firebase-tests/                  rules tests that run against the Firebase emulators
docs/                            Firebase setup, release process, code review notes
```

The screens never talk to Firebase directly. ViewModels depend on the repository interfaces, so all business logic is unit tested with fakes.

## Build and test

Requirements: JDK 17 and the Android SDK (API 36).

```
./gradlew testDebugUnitTest      # unit tests
./gradlew lintDebug              # Android lint
./gradlew assembleDebug          # debug APK
./gradlew assembleRelease        # R8 shrunk release build (unsigned without a keystore)

cd firebase-tests && npm install && npm test    # security rules tests (needs Java for the emulators)
```

GitHub Actions runs all of the above on every push. See `.github/workflows/android.yml`.

## Setting up your own Firebase project

`app/google-services.json` points at the existing ColFind Firebase project. To use another project, replace that file and follow [docs/FIREBASE_SETUP.md](docs/FIREBASE_SETUP.md). Deploying the security rules is required: without them the database is not protected.

## Releasing

See [docs/RELEASING.md](docs/RELEASING.md) for signing, the Play Store checklist and rollback triggers.
