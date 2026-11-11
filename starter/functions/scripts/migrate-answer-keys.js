const { initializeApp } = require("firebase-admin/app");
const { FieldValue, getFirestore } = require("firebase-admin/firestore");

const knownAnswerFields = ["correctIndex", "correctAnswer", "answerKey", "answerKeys"];
const mode = process.argv[2] || "--dry-run";
if (!["--dry-run", "--write-keys", "--remove-legacy"].includes(mode)) {
  throw new Error("Usage: node scripts/migrate-answer-keys.js [--dry-run|--write-keys|--remove-legacy]");
}

if (process.env.FIRESTORE_EMULATOR_HOST) {
  initializeApp({ projectId: process.env.GCLOUD_PROJECT || "demo-readventures-rules" });
} else {
  initializeApp();
}
const db = getFirestore();

function answerIndex(data) {
  const candidates = [];
  if (Number.isSafeInteger(data.correctIndex)) candidates.push(data.correctIndex);
  for (const field of ["correctAnswer", "answerKey"]) {
    const value = data[field];
    if (Number.isSafeInteger(value)) candidates.push(value);
    if (typeof value === "string" && Array.isArray(data.choices)) {
      const matches = data.choices.flatMap((choice, index) => choice === value ? [index] : []);
      if (matches.length === 1) candidates.push(matches[0]);
    }
  }
  if (Array.isArray(data.answerKeys) && data.answerKeys.length === 1) {
    const value = data.answerKeys[0];
    if (Number.isSafeInteger(value)) candidates.push(value);
    if (typeof value === "string" && Array.isArray(data.choices)) {
      const matches = data.choices.flatMap((choice, index) => choice === value ? [index] : []);
      if (matches.length === 1) candidates.push(matches[0]);
    }
  }
  return candidates.length > 0 && candidates.every((candidate) => candidate === candidates[0])
    ? candidates[0]
    : null;
}

async function chunks(items, callback) {
  for (let offset = 0; offset < items.length; offset += 400) {
    const batch = db.batch();
    items.slice(offset, offset + 400).forEach((item) => callback(batch, item));
    await batch.commit();
  }
}

async function main() {
  const snapshot = await db.collection("questions").get();
  const questions = snapshot.docs.map((document) => ({
    id: document.id,
    ref: document.ref,
    data: document.data()
  }));
  const materialIds = [...new Set(questions.map((question) => question.data.materialId).filter(Boolean))];
  const materialSnapshots = await Promise.all(
    materialIds.map((id) => db.collection("readingMaterials").doc(id).get())
  );
  const materials = new Map(materialSnapshots.map((document) => [document.id, document.data() || {}]));
  const plans = questions.map((question) => {
    const data = question.data;
    const answerFields = knownAnswerFields.filter((field) =>
      Object.prototype.hasOwnProperty.call(data, field));
    const suspiciousFields = Object.keys(data).filter((field) =>
      /correct|answer|key/i.test(field) && field !== "answerKeySeparated");
    const index = answerIndex(data);
    const choices = data.choices;
    const structurallyValid = typeof data.materialId === "string"
      && materials.has(data.materialId)
      && Array.isArray(choices)
      && choices.length >= 2
      && suspiciousFields.every((field) => knownAnswerFields.includes(field));
    const createdBy = typeof data.createdBy === "string"
      ? data.createdBy
      : (materials.get(data.materialId)?.createdBy || "migration");
    return {
      ...question,
      index,
      createdBy,
      answerFields,
      suspiciousFields,
      structurallyValid,
      valid: structurallyValid && Number.isSafeInteger(index) && index >= 0 && index < choices.length
    };
  });
  const keyRefs = plans.map((plan) => db.collection("questionAnswerKeys").doc(plan.id));
  const keySnapshots = await Promise.all(keyRefs.map((ref) => ref.get()));
  const existingKeys = new Map(keySnapshots.map((document) => [document.id, document.data()]));
  for (const plan of plans) {
    if (plan.answerFields.length === 0) {
      plan.index = existingKeys.get(plan.id)?.correctIndex;
      plan.valid = plan.structurallyValid
        && Number.isSafeInteger(plan.index)
        && plan.index >= 0
        && plan.index < plan.data.choices.length;
    }
  }

  const legacyPlans = plans.filter((plan) => plan.answerFields.length > 0);
  const unsupported = plans.filter((plan) =>
    plan.suspiciousFields.some((field) => !knownAnswerFields.includes(field)));
  const invalid = plans.filter((plan) => !plan.valid);
  console.log(`Mode: ${mode}`);
  console.log(`Question documents: ${plans.length}`);
  console.log(`Documents with recognized legacy answer fields: ${legacyPlans.length}`);
  console.log(`Documents requiring manual review: ${unsupported.length + invalid.length}`);
  console.log(`Existing protected answer-key documents: ${keySnapshots.filter((document) => document.exists).length}`);

  if (mode === "--dry-run") {
    for (const plan of legacyPlans) {
      console.log(
        `${plan.valid ? "Ready" : "Review"} ${plan.id}: materialId=${plan.data.materialId || "missing"}, ` +
        `answerIndex=${Number.isSafeInteger(plan.index) ? plan.index : "unresolved"}, ` +
        `fields=${plan.answerFields.join(",")}, key=${existingKeys.has(plan.id) ? "present" : "missing"}`
      );
    }
    for (const plan of [...new Set([...unsupported, ...invalid])]) {
      console.log(`Review ${plan.id}: fields=${plan.suspiciousFields.join(",") || "none"}, materialId=${plan.data.materialId || "missing"}`);
    }
    console.log("Dry run only. No documents were changed.");
    return;
  }
  if (unsupported.length > 0 || invalid.length > 0) {
    throw new Error("Migration stopped: resolve every malformed or unsupported answer field before applying changes.");
  }

  if (mode === "--write-keys") {
    const writes = [];
    for (const plan of plans.filter((item) => item.answerFields.length > 0)) {
      const current = existingKeys.get(plan.id);
      if (current) {
        if (current.materialId !== plan.data.materialId
          || current.correctIndex !== plan.index
          || current.createdBy !== plan.createdBy) {
          throw new Error(`Existing answer key conflicts with question ${plan.id}; no migration writes were committed.`);
        }
        continue;
      }
      writes.push(plan);
    }
    await chunks(writes, (batch, plan) => {
      batch.create(db.collection("questionAnswerKeys").doc(plan.id), {
        materialId: plan.data.materialId,
        correctIndex: plan.index,
        createdBy: plan.createdBy
      });
    });
    console.log(`Created ${writes.length} protected answer keys. Legacy question fields remain untouched.`);
    console.log("Run --dry-run again, then --remove-legacy only after verifying the keys and backups.");
    return;
  }

  for (const plan of plans) {
    const key = existingKeys.get(plan.id);
    if (!plan.valid || !key
      || key.materialId !== plan.data.materialId
      || key.correctIndex !== plan.index
      || key.createdBy !== plan.createdBy) {
      throw new Error(`Protected answer key missing or mismatched for ${plan.id}; no legacy fields were removed.`);
    }
  }
  await chunks(plans, (batch, plan) => {
    batch.update(plan.ref, {
      answerKeySeparated: true,
      correctIndex: FieldValue.delete(),
      correctAnswer: FieldValue.delete(),
      answerKey: FieldValue.delete(),
      answerKeys: FieldValue.delete()
    });
  });
  console.log(`Verified answer keys for all ${plans.length} questions, then removed legacy answer fields and enabled student reads.`);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
