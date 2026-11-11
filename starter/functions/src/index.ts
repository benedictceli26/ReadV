import { createHash } from "node:crypto";
import { initializeApp } from "firebase-admin/app";
import { FieldValue, getFirestore } from "firebase-admin/firestore";
import { HttpsError, onCall } from "firebase-functions/v2/https";

initializeApp();

const db = getFirestore();

type SubmittedAnswer = {
  questionId: string;
  selectedIndex: number;
};

type SafeResult = {
  score: number;
  total: number;
  percentage: number;
  passed: boolean;
  feedback: string;
  attemptId: string;
  scoreId: string;
};

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function parseAnswers(value: unknown): SubmittedAnswer[] {
  if (!Array.isArray(value) || value.length < 1 || value.length > 20) {
    throw new HttpsError("invalid-argument", "Submit between 1 and 20 answers.");
  }

  const seen = new Set<string>();
  return value.map((answer) => {
    if (!isRecord(answer)
      || typeof answer.questionId !== "string"
      || answer.questionId.length < 1
      || answer.questionId.length > 150
      || !Number.isSafeInteger(answer.selectedIndex)
      || (answer.selectedIndex as number) < 0
      || seen.has(answer.questionId)) {
      throw new HttpsError("invalid-argument", "One or more submitted answers are invalid.");
    }
    seen.add(answer.questionId);
    return {
      questionId: answer.questionId,
      selectedIndex: answer.selectedIndex as number
    };
  });
}

function feedback(percentage: number): string {
  if (percentage === 100) return "Perfect score! You understood the material well.";
  if (percentage >= 80) return "Great work! You passed this quiz.";
  if (percentage >= 60) return "Good effort. Review the material and try again.";
  return "Keep practicing. Review the material and try again.";
}

function safeResult(value: unknown): SafeResult {
  if (!isRecord(value)
    || typeof value.score !== "number"
    || typeof value.total !== "number"
    || typeof value.percentage !== "number"
    || typeof value.passed !== "boolean"
    || typeof value.feedback !== "string"
    || typeof value.attemptId !== "string"
    || typeof value.scoreId !== "string") {
    throw new HttpsError("internal", "The saved quiz result is invalid.");
  }
  return {
    score: value.score,
    total: value.total,
    percentage: value.percentage,
    passed: value.passed,
    feedback: value.feedback,
    attemptId: value.attemptId,
    scoreId: value.scoreId
  };
}

export const submitQuiz = onCall(async (request): Promise<SafeResult> => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Sign in before submitting a quiz.");
  }
  if (!isRecord(request.data)
    || typeof request.data.materialId !== "string"
    || request.data.materialId.length < 1
    || request.data.materialId.length > 150
    || typeof request.data.attemptId !== "string"
    || !/^[A-Za-z0-9_-]{16,80}$/.test(request.data.attemptId)) {
    throw new HttpsError("invalid-argument", "The quiz submission is malformed.");
  }

  const { materialId, attemptId } = request.data as {
    materialId: string;
    attemptId: string;
  };
  const answers = parseAnswers(request.data.answers);
  const userId = request.auth.uid;
  const materialSnapshot = await db.collection("readingMaterials").doc(materialId).get();
  if (!materialSnapshot.exists) {
    throw new HttpsError("not-found", "This reading material is unavailable.");
  }
  const material = materialSnapshot.data() ?? {};
  if (material.published === false || material.isPublished === false) {
    throw new HttpsError("failed-precondition", "This reading material is not available for quizzes.");
  }

  const questionsSnapshot = await db.collection("questions")
    .where("materialId", "==", materialId)
    .get();
  if (questionsSnapshot.empty) {
    throw new HttpsError("failed-precondition", "This material does not have a published quiz.");
  }

  const questionDocuments = questionsSnapshot.docs
    .map((document) => ({ id: document.id, data: document.data() }))
    .sort((left, right) => {
      const leftOrder = Number.isSafeInteger(left.data.questionOrder) ? left.data.questionOrder as number : 0;
      const rightOrder = Number.isSafeInteger(right.data.questionOrder) ? right.data.questionOrder as number : 0;
      return leftOrder - rightOrder || left.id.localeCompare(right.id);
    });

  if (questionDocuments.some(({ data }) => data.answerKeySeparated !== true)) {
    throw new HttpsError(
      "failed-precondition",
      "This quiz must be migrated to protected answer keys before it can be submitted."
    );
  }
  if (answers.length !== questionDocuments.length) {
    throw new HttpsError("invalid-argument", "Submit one answer for every quiz question.");
  }

  const questionById = new Map(questionDocuments.map((question) => [question.id, question]));
  for (const answer of answers) {
    const question = questionById.get(answer.questionId);
    if (!question) {
      throw new HttpsError("invalid-argument", "A submitted question does not belong to this quiz.");
    }
    if (!Array.isArray(question.data.choices)
      || question.data.choices.length < 2
      || answer.selectedIndex >= question.data.choices.length) {
      throw new HttpsError("invalid-argument", "A selected answer is outside the available choices.");
    }
  }

  const answerKeySnapshots = await Promise.all(
    questionDocuments.map(({ id }) =>
      db.collection("questionAnswerKeys").doc(id).get())
  );
  const answerKeyById = new Map(answerKeySnapshots.map((document) => [document.id, document]));
  let score = 0;
  for (const answer of answers) {
    const question = questionById.get(answer.questionId);
    const keyDocument = answerKeyById.get(answer.questionId);
    if (!question || !keyDocument?.exists) {
      throw new HttpsError("failed-precondition", "A quiz answer key is unavailable.");
    }
    const key = keyDocument.data() ?? {};
    if (key.materialId !== materialId
      || !Number.isSafeInteger(key.correctIndex)
      || (key.correctIndex as number) < 0
      || (key.correctIndex as number) >= (question.data.choices as unknown[]).length
      || (typeof question.data.createdBy === "string" && key.createdBy !== question.data.createdBy)) {
      throw new HttpsError("failed-precondition", "A quiz answer key is invalid.");
    }
    if (answer.selectedIndex === key.correctIndex) score++;
  }

  const percentage = Math.floor((score * 100) / answers.length);
  const resultData: SafeResult = {
    score,
    total: answers.length,
    percentage,
    passed: percentage >= 80,
    feedback: feedback(percentage),
    attemptId,
    scoreId: createHash("sha256").update(`${userId}:${attemptId}`).digest("hex")
  };
  const answerDigest = createHash("sha256")
    .update(JSON.stringify([...answers].sort((a, b) => a.questionId.localeCompare(b.questionId))))
    .digest("hex");
  const attemptRef = db.collection("quizAttempts").doc(resultData.scoreId);
  const scoreRef = db.collection("scores").doc(resultData.scoreId);

  return db.runTransaction(async (transaction) => {
    const existing = await transaction.get(attemptRef);
    if (existing.exists) {
      const stored = existing.data() ?? {};
      if (stored.userId !== userId || stored.answerDigest !== answerDigest) {
        throw new HttpsError("already-exists", "This attempt identifier was already used.");
      }
      return safeResult(stored.result);
    }

    transaction.create(attemptRef, {
      userId,
      materialId,
      attemptId,
      answerDigest,
      result: resultData,
      submittedAt: FieldValue.serverTimestamp()
    });
    transaction.create(scoreRef, {
      userId,
      materialId,
      materialTitle: typeof material.title === "string" ? material.title : "",
      score,
      total: answers.length,
      percentage,
      passed: resultData.passed,
      date: Date.now(),
      attemptId
    });
    return resultData;
  });
});
