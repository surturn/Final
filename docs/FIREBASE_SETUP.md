# Firebase setup and administration

## One time setup

1. In the Firebase console, enable **Authentication > Sign-in method > Email/Password**.
2. Turn on **email enumeration protection** (Authentication > Settings > User actions).
3. Create the Firestore database and the default Storage bucket.
4. Deploy rules and indexes from the repository root:
   ```
   npm install -g firebase-tools
   firebase login
   firebase use <your-project-id>
   firebase deploy --only firestore:rules,firestore:indexes,storage
   ```
5. In Google Cloud console > APIs and Services > Credentials, restrict the Android API key to the package `com.sydney.sydney` and your release SHA-1 and Play App Signing SHA-1.
6. Set a budget alert on the project so a runaway client cannot surprise you with a bill.

## Data model

| Path | Who reads | Who writes | Notes |
| --- | --- | --- | --- |
| `users/{uid}` | the owner and admins | the owner (cannot approve themselves) | name, email, campus, phone, area, `landlordStatus`, `createdAt` |
| `students/{uid}` | any signed-in user | the owner | directory entry, no phone or email |
| `hostels/{id}` | everyone | approved landlords, for their own listings | `priceKes` is a number, `contactPhone` is required |
| `admins/{uid}` | the owner | console only | marker document that makes a user an admin |

Storage: `students/{uid}/*.jpg` (signed-in read, owner write) and `hostels/{uid}/*.jpg` (public read, approved landlord write). Images only, under 5 MB. The app shrinks photos to 1280 px before upload.

## Approving a landlord

1. Open Firestore > `users` and find the document of the applicant (their email is in the document).
2. Check that `landlordStatus` is `pending`.
3. Change `landlordStatus` to `approved` (or `rejected`).

The app re-reads the status when the user returns to the home screen. The console bypasses security rules, so this is the only way to approve. Nobody can approve themselves from the app.

## Making someone an admin

Create the document `admins/<their uid>` in the console with any field, for example `createdAt`. Admins can read user records and delete hostel listings.

## Moving from the old app version

Earlier builds wrote to the capitalised `Students` collection, stored hostel prices as text and uploaded photos to `images/` and `hostels/<id>.jpg`. The new app ignores those documents (listings without a numeric `priceKes` are skipped). Export anything you want to keep, then delete the old `Students` collection and the old documents in `hostels`. Existing users keep working because Firebase Auth accounts are unchanged, but they have no `users` record: the app asks them to complete their profile the first time they open **My profile**.

## Running the rules tests

```
cd firebase-tests
npm install
npm test
```

This starts the Firestore and Storage emulators and runs the tests in `rules.test.mjs`.
