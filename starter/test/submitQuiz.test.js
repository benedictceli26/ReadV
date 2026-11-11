const fs = require('fs');
const {
  initializeTestEnvironment
} = require('@firebase/rules-unit-testing');
const { collection, doc, getDoc, getDocs, setDoc } = require('firebase/firestore');
const { assertFails, assertSucceeds } = require('@firebase/rules-unit-testing');
const { initializeApp, deleteApp } = require('firebase/app');
const { connectAuthEmulator, getAuth, signInAnonymously, signOut } = require('firebase/auth');
const { connectFunctionsEmulator, getFunctions, httpsCallable } = require('firebase/functions');
const { after, before, beforeEach, describe, it } = require('mocha');

const projectId = 'demo-readventures-rules';
let env;
let app;
let auth;
let submitQuiz;

const prompt = (order, text) => ({
  materialId: 'quiz-story',
  question: text,
  choices: ['A', 'B', 'C', 'D'],
  questionOrder: order,
  createdBy: 'teacherA',
  isTeacherMade: true,
  answerKeySeparated: true
});

before(async () => {
  env = await initializeTestEnvironment({
    projectId,
    firestore: { rules: fs.readFileSync('firestore.rules', 'utf8') }
  });
  app = initializeApp({ projectId, apiKey: 'fake-api-key' }, 'submit-quiz-emulator-tests');
  auth = getAuth(app);
  connectAuthEmulator(auth, 'http://127.0.0.1:9099', { disableWarnings: true });
  const functions = getFunctions(app, 'us-central1');
  connectFunctionsEmulator(functions, '127.0.0.1', 5001);
  submitQuiz = httpsCallable(functions, 'submitQuiz');
});

beforeEach(async () => {
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await setDoc(doc(db, 'readingMaterials/quiz-story'), {
      title: 'Emulator Quiz',
      createdBy: 'teacherA',
      isTeacherMade: true,
      published: true
    });
    await setDoc(doc(db, 'questions/question-one'), prompt(0, 'First?'));
    await setDoc(doc(db, 'questions/question-two'), prompt(1, 'Second?'));
    await setDoc(doc(db, 'questionAnswerKeys/question-one'), {
      materialId: 'quiz-story',
      correctIndex: 1,
      createdBy: 'teacherA'
    });
    await setDoc(doc(db, 'questionAnswerKeys/question-two'), {
      materialId: 'quiz-story',
      correctIndex: 3,
      createdBy: 'teacherA'
    });
  });
  if (auth.currentUser) await signOut(auth);
});

after(async () => {
  if (app) await deleteApp(app);
  if (env) await env.cleanup();
});

describe('submitQuiz callable', () => {
  const answers = [
    { questionId: 'question-one', selectedIndex: 1 },
    { questionId: 'question-two', selectedIndex: 2 }
  ];

  it('rejects unauthenticated submissions', async () => {
    await submitQuiz({
      materialId: 'quiz-story',
      answers,
      attemptId: 'attempt-unauthenticated-01'
    }).then(
      () => { throw new Error('Expected unauthenticated request to fail.'); },
      (error) => {
        if (error.code !== 'functions/unauthenticated') throw error;
      }
    );
  });

  it('rejects question IDs outside the selected material and invalid choice indexes', async () => {
    await signInAnonymously(auth);
    for (const invalidAnswers of [
      [{ questionId: 'not-this-quiz', selectedIndex: 0 }, answers[1]],
      [{ questionId: 'question-one', selectedIndex: 4 }, answers[1]],
      [answers[0]]
    ]) {
      await submitQuiz({
        materialId: 'quiz-story',
        answers: invalidAnswers,
        attemptId: `invalid-attempt-${Math.random().toString(36).slice(2, 12)}`
      }).then(
        () => { throw new Error('Expected malformed answer set to fail.'); },
        (error) => {
          if (error.code !== 'functions/invalid-argument') throw error;
        }
      );
    }
  });

  it('returns only safe score fields and saves one idempotent result', async () => {
    await signInAnonymously(auth);
    const payload = {
      materialId: 'quiz-story',
      answers,
      attemptId: 'attempt-safe-result-0001'
    };
    const first = (await submitQuiz(payload)).data;
    const second = (await submitQuiz(payload)).data;
    if (JSON.stringify(first) !== JSON.stringify(second)) {
      throw new Error('Duplicate attempt did not return the same result.');
    }
    if (first.score !== 1 || first.total !== 2 || first.percentage !== 50 || first.passed !== false) {
      throw new Error(`Unexpected trusted result: ${JSON.stringify(first)}`);
    }
    if (Object.keys(first).some((key) => /correctIndex|answerKey|choices|question/i.test(key))) {
      throw new Error(`Callable result included sensitive fields: ${JSON.stringify(first)}`);
    }
    const ownDb = env.authenticatedContext(auth.currentUser.uid).firestore();
    await assertSucceeds(getDoc(doc(ownDb, 'scores', first.scoreId)));
    await assertFails(getDoc(doc(env.authenticatedContext('another-student').firestore(), 'scores', first.scoreId)));
    let scores;
    await env.withSecurityRulesDisabled(async (context) => {
      scores = await getDocs(collection(context.firestore(), 'scores'));
    });
    if (scores.size !== 1) throw new Error(`Expected one persisted result, found ${scores.size}.`);
    await submitQuiz({
      ...payload,
      answers: [
        { questionId: 'question-one', selectedIndex: 0 },
        answers[1]
      ]
    }).then(
      () => { throw new Error('Expected reused attempt ID with changed answers to fail.'); },
      (error) => {
        if (error.code !== 'functions/already-exists') throw error;
      }
    );
  });
});
