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

test("Unauthenticated user: cannot read accounts", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("accounts").get());
});

test("Authenticated user: can create their own account and profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      displayName: "Alice",
      currency: "BDT",
      language: "en",
      themeMode: "dark"
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("accounts").doc("bkash_acc").set({
      name: "bKash Personal",
      type: "BKASH",
      balance: 1500,
      accountNumber: "01700000000"
    })
  );
});

test("Authenticated user: cannot read another user's accounts", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("accounts").doc("secret_acc").set({
      name: "Alice Secret Bank",
      type: "BANK",
      balance: 50000
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("accounts").doc("secret_acc").get()
  );
});

test("Authenticated user: cannot write into another user's transactions", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("transactions").doc("txn1").set({
      title: "Hacked Txn",
      amount: 100,
      type: "SPENT",
      category: "Food",
      accountId: "acc1",
      accountName: "Cash",
      date: "2026-10-23"
    })
  );
});
