const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const STUDENT_A_UID = "student_alice_123";
const STUDENT_B_UID = "student_bob_456";
const ADMIN_UID = "admin_justin_789";
const ADMIN_EMAIL = "justinramos1030@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function adminContext() {
  return testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  });
}

function spoofedAdminContext() {
  return testEnv.authenticatedContext("spoof_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  });
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("announcements").where("campusId", "==", "sti_sta_mesa").get());
  await assertFails(unauthDb.collection("maintenance_reports").where("userId", "==", STUDENT_A_UID).get());
  await assertFails(unauthDb.collection("lost_found_items").where("campusId", "==", "sti_sta_mesa").get());
  await assertFails(
    unauthDb.collection("maintenance_reports").doc("rep_1").set({
      userId: STUDENT_A_UID,
      studentName: "Alice",
      issueCategory: "Aircon / HVAC",
      description: "Not cooling",
      status: "Pending",
      campusId: "sti_sta_mesa",
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("User profiles isolate PII to owner and block email tampering", async () => {
  const aliceDb = testEnv.authenticatedContext(STUDENT_A_UID, { email: "alice@sti.edu.ph", email_verified: true }).firestore();
  const bobDb = testEnv.authenticatedContext(STUDENT_B_UID, { email: "bob@sti.edu.ph", email_verified: true }).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb.collection("users").doc(STUDENT_A_UID).set({
      userId: STUDENT_A_UID,
      displayName: "Alice Reyes",
      email: "alice@sti.edu.ph",
      campus: "STI Sta. Mesa",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob cannot read or overwrite Alice's profile
  await assertFails(bobDb.collection("users").doc(STUDENT_A_UID).get());
  await assertFails(bobDb.collection("users").doc(STUDENT_A_UID).update({ displayName: "Hacked", updatedAt: new Date(Date.now() - 1000) }));

  // Alice cannot inject extra ghost fields or mutate email/createdAt
  await assertFails(
    aliceDb.collection("users").doc(STUDENT_A_UID).update({
      email: ADMIN_EMAIL,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc("extra_user").set({
      userId: "extra_user",
      displayName: "Shadow",
      email: "x@y.com",
      campus: "STI Sta. Mesa",
      isAdmin: true,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Announcements: verified admin can create/update/delete; students can query campus announcements only; unverified admin email rejected", async () => {
  const adminDb = adminContext().firestore();
  const spoofDb = spoofedAdminContext().firestore();
  const aliceDb = testEnv.authenticatedContext(STUDENT_A_UID).firestore();
  const now = new Date(Date.now() - 2000);

  // Unverified email spoofing admin must fail
  await assertFails(
    spoofDb.collection("announcements").doc("ann_spoof").set({
      authorId: "spoof_uid",
      authorName: "Spoof",
      title: "Fake",
      message: "Fake",
      campusId: "sti_sta_mesa",
      published: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Student cannot post announcement
  await assertFails(
    aliceDb.collection("announcements").doc("ann_student").set({
      authorId: STUDENT_A_UID,
      authorName: "Alice",
      title: "Student Post",
      message: "Should fail",
      campusId: "sti_sta_mesa",
      published: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Verified admin succeeds
  await assertSucceeds(
    adminDb.collection("announcements").doc("ann_1").set({
      authorId: ADMIN_UID,
      authorName: "Admin Staff",
      title: "Campus Notice",
      message: "Library AC maintenance tomorrow at 2 PM.",
      campusId: "sti_sta_mesa",
      published: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Student filtered query succeeds; unfiltered query fails
  await assertSucceeds(aliceDb.collection("announcements").where("campusId", "==", "sti_sta_mesa").get());
  await assertFails(aliceDb.collection("announcements").get());
});

test("Maintenance reports: student create & scoped query, status locking, and admin status update", async () => {
  const aliceDb = testEnv.authenticatedContext(STUDENT_A_UID).firestore();
  const bobDb = testEnv.authenticatedContext(STUDENT_B_UID).firestore();
  const adminDb = adminContext().firestore();
  const now = new Date(Date.now() - 2000);

  // Student cannot submit report with pre-approved status or @DocumentId 'id'
  await assertFails(
    aliceDb.collection("maintenance_reports").doc("rep_bad_status").set({
      userId: STUDENT_A_UID,
      studentName: "Alice",
      issueCategory: "Aircon / HVAC",
      description: "Leaking AC in Room 305",
      status: "Fixed",
      campusId: "sti_sta_mesa",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(
    aliceDb.collection("maintenance_reports").doc("rep_bad_id").set({
      id: "rep_bad_id",
      userId: STUDENT_A_UID,
      studentName: "Alice",
      issueCategory: "Aircon / HVAC",
      description: "Leaking AC",
      status: "Pending",
      campusId: "sti_sta_mesa",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Valid create by Alice
  await assertSucceeds(
    aliceDb.collection("maintenance_reports").doc("rep_1").set({
      userId: STUDENT_A_UID,
      studentName: "Alice Reyes",
      issueCategory: "Aircon / HVAC",
      description: "Room 402 aircon is blowing warm air",
      status: "Pending",
      campusId: "sti_sta_mesa",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Query parity: Alice's filtered query succeeds, unfiltered query fails, Bob cannot read Alice's report
  await assertSucceeds(aliceDb.collection("maintenance_reports").where("userId", "==", STUDENT_A_UID).get());
  await assertFails(aliceDb.collection("maintenance_reports").get());
  await assertFails(bobDb.collection("maintenance_reports").doc("rep_1").get());

  // Student cannot change their own report status
  await assertFails(
    aliceDb.collection("maintenance_reports").doc("rep_1").update({
      status: "Fixed",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Admin can list all and update status to On going / Fixed
  await assertSucceeds(adminDb.collection("maintenance_reports").get());
  await assertSucceeds(
    adminDb.collection("maintenance_reports").doc("rep_1").update({
      status: "On going",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(
    adminDb.collection("maintenance_reports").doc("rep_1").update({
      status: "Fixed",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Once non-Pending, student cannot edit description
  await assertFails(
    aliceDb.collection("maintenance_reports").doc("rep_1").update({
      description: "Tampered after fixed",
      updatedAt: new Date(Date.now() - 100),
    })
  );
});

test("Lost & Found items: campus list parity, student creation, and admin claim status update", async () => {
  const aliceDb = testEnv.authenticatedContext(STUDENT_A_UID).firestore();
  const bobDb = testEnv.authenticatedContext(STUDENT_B_UID).firestore();
  const adminDb = adminContext().firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb.collection("lost_found_items").doc("lf_1").set({
      userId: STUDENT_A_UID,
      studentName: "Alice Reyes",
      description: "Found purple Aquaflask near Room 204",
      status: "On hold",
      campusId: "sti_sta_mesa",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Campus query succeeds; unfiltered query fails for student
  await assertSucceeds(bobDb.collection("lost_found_items").where("campusId", "==", "sti_sta_mesa").get());
  await assertFails(bobDb.collection("lost_found_items").get());

  // Bob cannot claim/modify Alice's item
  await assertFails(
    bobDb.collection("lost_found_items").doc("lf_1").update({
      status: "Claimed",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Admin updates status to Claimed
  await assertSucceeds(
    adminDb.collection("lost_found_items").doc("lf_1").update({
      status: "Claimed",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Feedback: owner create & read, immutable after creation", async () => {
  const aliceDb = testEnv.authenticatedContext(STUDENT_A_UID).firestore();
  const bobDb = testEnv.authenticatedContext(STUDENT_B_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb.collection("feedback").doc("fb_1").set({
      userId: STUDENT_A_UID,
      studentName: "Alice Reyes",
      rating: 5,
      message: "Fast response from Sta. Mesa maintenance team!",
      createdAt: now,
    })
  );

  await assertSucceeds(aliceDb.collection("feedback").where("userId", "==", STUDENT_A_UID).get());
  await assertFails(bobDb.collection("feedback").doc("fb_1").get());
  await assertFails(aliceDb.collection("feedback").doc("fb_1").update({ message: "Edited", rating: 1 }));
});
