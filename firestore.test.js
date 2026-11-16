const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ADMIN_UID = "admin_danylo";
const ADMIN_EMAIL = "danyloandtadik@gmail.com";
const USER_UID = "friend_user_1";
const FRIEND_EMAIL = "friend@example.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validReleasePayload(authorId, overrides = {}) {
  const pastTime = new Date(Date.now() - 1000);
  return {
    authorId,
    targetVersion: "2.1",
    releaseNotes: "Оновлено дизайн, віджети та хмарну доставку релізів.",
    appTitle: "Danny Weather",
    accentTheme: "SKY",
    isAvailable: true,
    createdAt: pastTime,
    updatedAt: pastTime,
    ...overrides,
  };
}

function validVipPayload(authorId, email, overrides = {}) {
  const pastTime = new Date(Date.now() - 1000);
  return {
    authorId,
    email,
    createdAt: pastTime,
    ...overrides,
  };
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

test("Unauthenticated user: can read latest release & theme, cannot write app_releases or read vip_access", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("app_releases").doc("latest").set(validReleasePayload(ADMIN_UID));
    await ctx.firestore().collection("vip_access").doc("vip_friend").set(validVipPayload(ADMIN_UID, FRIEND_EMAIL));
  });

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauthDb.collection("app_releases").doc("latest").get());
  await assertSucceeds(unauthDb.collection("app_releases").where("isAvailable", "==", true).get());
  await assertFails(unauthDb.collection("app_releases").get());
  await assertFails(
    unauthDb.collection("app_releases").doc("latest").set(validReleasePayload("anon"))
  );
  await assertFails(unauthDb.collection("vip_access").doc("vip_friend").get());
  await assertFails(unauthDb.collection("vip_access").get());
});

test("Spoofed unverified admin email: rejected on write and vip_access list", async () => {
  const spoofDb = testEnv.authenticatedContext("spoof_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();

  await assertFails(
    spoofDb.collection("app_releases").doc("latest").set(validReleasePayload("spoof_uid"))
  );
  await assertFails(
    spoofDb.collection("vip_access").doc("vip_1").set(validVipPayload("spoof_uid", FRIEND_EMAIL))
  );
  await assertFails(spoofDb.collection("vip_access").get());
});

test("Regular signed-in user: cannot create, update, or delete app_releases or vip_access", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("app_releases").doc("latest").set(validReleasePayload(ADMIN_UID));
  });

  const userDb = testEnv.authenticatedContext(USER_UID, {
    email: FRIEND_EMAIL,
    email_verified: true,
  }).firestore();

  await assertFails(
    userDb.collection("app_releases").doc("custom_id").set(validReleasePayload(USER_UID))
  );
  await assertFails(
    userDb.collection("app_releases").doc("latest").update({
      targetVersion: "9.9",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(userDb.collection("app_releases").doc("latest").delete());
  await assertFails(
    userDb.collection("vip_access").doc("vip_self").set(validVipPayload(USER_UID, FRIEND_EMAIL))
  );
});

test("Regular signed-in user: can query only their own vip_access email, never others or unfiltered list", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("vip_access").doc("vip_friend").set(validVipPayload(ADMIN_UID, FRIEND_EMAIL));
    await ctx.firestore().collection("vip_access").doc("vip_other").set(validVipPayload(ADMIN_UID, "secret@other.com"));
  });

  const friendDb = testEnv.authenticatedContext(USER_UID, {
    email: FRIEND_EMAIL,
    email_verified: true,
  }).firestore();

  await assertSucceeds(friendDb.collection("vip_access").where("email", "==", FRIEND_EMAIL).get());
  await assertFails(friendDb.collection("vip_access").get());
  await assertFails(friendDb.collection("vip_access").doc("vip_other").get());
});

test("Regular signed-in user: cannot list admins or write admins", async () => {
  const userDb = testEnv.authenticatedContext(USER_UID).firestore();
  await assertFails(userDb.collection("admins").get());
  await assertFails(userDb.collection("admins").doc(USER_UID).set({ grantedBy: "self" }));
});

test("Verified bootstrap admin: can create, update accentTheme & release, list/delete vip_access", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    adminDb.collection("app_releases").doc("latest").set(validReleasePayload(ADMIN_UID))
  );

  await assertSucceeds(
    adminDb.collection("app_releases").doc("latest").update({
      accentTheme: "EMERALD",
      targetVersion: "2.2",
      releaseNotes: "Покращено контраст і віджет.",
      isAvailable: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertSucceeds(
    adminDb.collection("vip_access").doc("vip_friend").set(validVipPayload(ADMIN_UID, FRIEND_EMAIL))
  );
  await assertSucceeds(adminDb.collection("vip_access").get());
  await assertSucceeds(adminDb.collection("vip_access").doc("vip_friend").delete());
  await assertSucceeds(adminDb.collection("app_releases").doc("latest").delete());
});

test("Shadow update (ghost field) and value poisoning rejected even for admin", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  await assertFails(
    adminDb.collection("app_releases").doc("latest").set({
      ...validReleasePayload(ADMIN_UID),
      isVerified: true,
    })
  );

  await assertFails(
    adminDb.collection("app_releases").doc("latest").set(
      validReleasePayload(ADMIN_UID, { targetVersion: "x".repeat(40) })
    )
  );

  await assertFails(
    adminDb.collection("vip_access").doc("vip_bad").set(validVipPayload(ADMIN_UID, "not-an-email"))
  );
});
