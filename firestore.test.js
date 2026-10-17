const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

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

// --- SECURITY BOUNDS TESTS ---

test("Unauthenticated user: cannot read notes", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("notes").get());
});

test("Authenticated user: cannot read another user's note", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("notes").doc("bob_note_1").set({
      userId: BOB_UID,
      title: "Secret Strategy",
      content: "Business plans",
      category: "BUSINESS",
      isPinned: false,
      isFavorite: false,
      colorHex: "#FFD700",
      tags: ["Strategy"],
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("notes").doc("bob_note_1").get());
});

test("Authenticated user: can create valid Business note", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_biz_1").set({
      userId: ALICE_UID,
      title: "Quarterly Review",
      content: "Meet with sales lead",
      category: "BUSINESS",
      isPinned: true,
      isFavorite: false,
      colorHex: "#1E293B",
      tags: ["Q3", "Sales"],
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: can create valid Private note", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_priv_1").set({
      userId: ALICE_UID,
      title: "Private Thoughts",
      content: "Personal journal entry",
      category: "PRIVATE",
      isPinned: false,
      isFavorite: true,
      colorHex: "#6366F1",
      tags: ["Personal"],
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: cannot create note with invalid category", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_invalid").set({
      userId: ALICE_UID,
      title: "Invalid",
      content: "Invalid category",
      category: "SPAM",
      isPinned: false,
      isFavorite: false,
      colorHex: "#000000",
      tags: [],
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: cannot create note with another user's userId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_spoof").set({
      userId: BOB_UID,
      title: "Spoofed",
      content: "Testing spoofing",
      category: "BUSINESS",
      isPinned: false,
      isFavorite: false,
      colorHex: "#000000",
      tags: [],
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: cannot delete another user's note", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("notes").doc("bob_note_2").set({
      userId: BOB_UID,
      title: "Bob's Note",
      content: "Confidential",
      category: "PRIVATE",
      isPinned: false,
      isFavorite: false,
      colorHex: "#000000",
      tags: [],
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("notes").doc("bob_note_2").delete());
});
