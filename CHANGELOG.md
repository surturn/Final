# Changelog

## 1.0.0

First release candidate. A full rewrite of the original student project.

### Security
- Added Firestore and Storage security rules with tests. Before, all data was open to the app's anyone.
- Phone numbers and emails are no longer stored in the public directory. Private details live in `users/{uid}`, readable only by their owner.
- Landlords must be approved by an admin before they can publish hostels. Users cannot approve themselves.
- Removed the contacts and storage permissions the app never used and turned off Android backup of local data.
- Sign in gating: directory, profile and add-hostel screens are no longer reachable when signed out.

### Fixed
- Hostel details read the wrong collection (`live`) and crashed on missing fields.
- Hostel prices were stored as text, so they could not be sorted or validated.
- The student dashboard read a document that was never created, so it always showed nothing.
- Registration checked for existing emails with a deprecated call that no longer returns anything, and created student records with random ids unrelated to the account.
- Forms were cleared before the upload finished, losing what the user typed on failure.
- "More hostels" revealed one hostel at a time. Unbounded `get()` reads fetched whole collections just to count students.
- Splash waited 3 seconds and stayed on the back stack. Back navigation always jumped to Home.
- Photos from phone cameras were uploaded at full size and ignored rotation.
- Light text on white backgrounds made several screens unreadable.

### Changed
- Material 3 only (Material 2 removed), consistent light and dark theme.
- ViewModels and repositories replace Firebase calls inside composables.
- Lists are capped (50 hostels, 500 students), searched locally and use stable keys.
- Photos are compressed to 1280 px before upload; replaced photos are deleted.
- Targets Android 16 (API 36), required by Google Play.

### Added
- Landlord contact number on every hostel with a call button.
- Landlord application flow, student profile editing, in-app account deletion.
- Unit tests for validators, filters and every ViewModel; rules tests for Firestore and Storage; CI for all of them.
