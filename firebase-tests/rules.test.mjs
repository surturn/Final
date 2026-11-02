import { readFileSync } from "node:fs";
import { after, before, beforeEach, describe, it } from "node:test";
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} from "@firebase/rules-unit-testing";
import { deleteDoc, doc, getDoc, serverTimestamp, setDoc, updateDoc } from "firebase/firestore";
import { ref, uploadBytes } from "firebase/storage";

const PROJECT_ID = "demo-colfind";
let env;

before(async () => {
  env = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: { rules: readFileSync("../firestore.rules", "utf8"), host: "127.0.0.1", port: 8080 },
    storage: { rules: readFileSync("../storage.rules", "utf8"), host: "127.0.0.1", port: 9199 },
  });
});

after(async () => {
  await env.cleanup();
});

beforeEach(async () => {
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await setDoc(doc(db, "users/landlord"), user("landlord@example.com", "approved"));
    await setDoc(doc(db, "users/pending"), user("pending@example.com", "pending"));
    await setDoc(doc(db, "users/student"), user("student@example.com", "none"));
    await setDoc(doc(db, "admins/admin"), { createdAt: 1 });
    await setDoc(doc(db, "hostels/h1"), hostel("landlord"));
  });
});

function user(email, landlordStatus) {
  return { displayName: "Test User", email, landlordStatus, createdAt: 1 };
}

function hostel(ownerId, overrides = {}) {
  return {
    name: "Sunrise Hostel",
    description: "Single rooms close to the campus gate with water and wifi.",
    priceKes: 6500,
    contactPhone: "0712345678",
    imageUrl: "https://example.com/h.jpg",
    ownerId,
    createdAt: 1,
    ...overrides,
  };
}

const as = (uid, email = `${uid}@example.com`) =>
  env.authenticatedContext(uid, { email });

describe("users", () => {
  it("lets a user create their own record as none or pending", async () => {
    const db = as("new").firestore();
    await assertSucceeds(setDoc(doc(db, "users/new"), user("new@example.com", "none")));
    await assertSucceeds(setDoc(doc(db, "users/new"), user("new@example.com", "pending")));
  });

  it("lets a user delete their own record but nobody else's", async () => {
    await assertFails(deleteDoc(doc(as("student").firestore(), "users/pending")));
    await assertSucceeds(deleteDoc(doc(as("student").firestore(), "users/student")));
  });

  it("rejects creating a record that is already approved", async () => {
    const db = as("new").firestore();
    await assertFails(setDoc(doc(db, "users/new"), user("new@example.com", "approved")));
  });

  it("rejects creating a record under another uid", async () => {
    const db = as("mallory").firestore();
    await assertFails(setDoc(doc(db, "users/new"), user("mallory@example.com", "none")));
  });

  it("hides a record from other users and anonymous visitors", async () => {
    await assertFails(getDoc(doc(as("other").firestore(), "users/student")));
    await assertFails(getDoc(doc(env.unauthenticatedContext().firestore(), "users/student")));
    await assertSucceeds(getDoc(doc(as("student").firestore(), "users/student")));
  });

  it("lets a student apply to be a landlord", async () => {
    const db = as("student").firestore();
    await assertSucceeds(updateDoc(doc(db, "users/student"), { landlordStatus: "pending" }));
  });

  it("stops a user approving themselves", async () => {
    await assertFails(
      updateDoc(doc(as("pending").firestore(), "users/pending"), { landlordStatus: "approved" })
    );
    await assertFails(
      updateDoc(doc(as("student").firestore(), "users/student"), { landlordStatus: "approved" })
    );
  });

  it("does not let a user change their email", async () => {
    await assertFails(
      updateDoc(doc(as("student").firestore(), "users/student"), { email: "other@example.com" })
    );
  });

  it("lets an admin approve a landlord but nothing else", async () => {
    const db = as("admin").firestore();
    await assertSucceeds(updateDoc(doc(db, "users/pending"), { landlordStatus: "approved" }));
    await assertFails(updateDoc(doc(db, "users/pending"), { displayName: "Renamed" }));
  });
});

describe("students directory", () => {
  const entry = { displayName: "Amina Otieno", campus: "MMU", createdAt: 1 };

  it("is readable by signed-in users only", async () => {
    await env.withSecurityRulesDisabled((ctx) => setDoc(doc(ctx.firestore(), "students/student"), entry));
    await assertSucceeds(getDoc(doc(as("anyone").firestore(), "students/student")));
    await assertFails(getDoc(doc(env.unauthenticatedContext().firestore(), "students/student")));
  });

  it("is writable by its owner only", async () => {
    await assertSucceeds(setDoc(doc(as("student").firestore(), "students/student"), entry));
    await assertFails(setDoc(doc(as("mallory").firestore(), "students/student"), entry));
  });

  it("refuses phone and email fields", async () => {
    const db = as("student").firestore();
    await assertFails(setDoc(doc(db, "students/student"), { ...entry, phone: "0700000000" }));
    await assertFails(setDoc(doc(db, "students/student"), { ...entry, email: "a@b.com" }));
  });
});

describe("hostels", () => {
  it("is publicly readable", async () => {
    await assertSucceeds(getDoc(doc(env.unauthenticatedContext().firestore(), "hostels/h1")));
  });

  it("can only be created by an approved landlord", async () => {
    await assertSucceeds(setDoc(doc(as("landlord").firestore(), "hostels/h2"), hostel("landlord")));
    await assertFails(setDoc(doc(as("pending").firestore(), "hostels/h3"), hostel("pending")));
    await assertFails(setDoc(doc(as("student").firestore(), "hostels/h4"), hostel("student")));
    await assertFails(setDoc(doc(env.unauthenticatedContext().firestore(), "hostels/h5"), hostel("x")));
  });

  it("must be owned by the creator", async () => {
    await assertFails(setDoc(doc(as("landlord").firestore(), "hostels/h6"), hostel("someone-else")));
  });

  it("rejects invalid prices and short descriptions", async () => {
    const db = as("landlord").firestore();
    await assertFails(setDoc(doc(db, "hostels/h7"), hostel("landlord", { priceKes: -5 })));
    await assertFails(setDoc(doc(db, "hostels/h8"), hostel("landlord", { priceKes: "6500" })));
    await assertFails(setDoc(doc(db, "hostels/h9"), hostel("landlord", { description: "short" })));
  });

  it("requires a contact phone so students can reach the landlord", async () => {
    const db = as("landlord").firestore();
    const { contactPhone, ...withoutPhone } = hostel("landlord");
    await assertFails(setDoc(doc(db, "hostels/h10"), withoutPhone));
    await assertFails(setDoc(doc(db, "hostels/h11"), hostel("landlord", { contactPhone: "123" })));
  });

  it("can only be edited and deleted by its owner", async () => {
    await assertSucceeds(updateDoc(doc(as("landlord").firestore(), "hostels/h1"), { priceKes: 7000 }));
    await assertFails(updateDoc(doc(as("student").firestore(), "hostels/h1"), { priceKes: 1 }));
    await assertFails(deleteDoc(doc(as("student").firestore(), "hostels/h1")));
    await assertSucceeds(deleteDoc(doc(as("landlord").firestore(), "hostels/h1")));
  });

  it("cannot be handed to another owner", async () => {
    await assertFails(updateDoc(doc(as("landlord").firestore(), "hostels/h1"), { ownerId: "student" }));
  });
});

describe("storage", () => {
  const png = new Uint8Array([137, 80, 78, 71]);
  const image = { contentType: "image/png" };

  it("lets an approved landlord upload hostel images", async () => {
    const storage = as("landlord").storage();
    await assertSucceeds(uploadBytes(ref(storage, "hostels/landlord/a.png"), png, image));
  });

  it("blocks unapproved users from the hostels folder", async () => {
    await assertFails(uploadBytes(ref(as("student").storage(), "hostels/student/a.png"), png, image));
  });

  it("blocks non-image uploads", async () => {
    const storage = as("landlord").storage();
    await assertFails(
      uploadBytes(ref(storage, "hostels/landlord/a.txt"), new Uint8Array([1]), { contentType: "text/plain" })
    );
  });

  it("only lets users write to their own student folder", async () => {
    await assertSucceeds(uploadBytes(ref(as("student").storage(), "students/student/me.png"), png, image));
    await assertFails(uploadBytes(ref(as("mallory").storage(), "students/student/me.png"), png, image));
  });
});
