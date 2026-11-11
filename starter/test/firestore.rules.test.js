const fs = require('fs');
const { initializeTestEnvironment, assertFails, assertSucceeds } = require('@firebase/rules-unit-testing');
const { collection, doc, getDoc, getDocs, query, setDoc, updateDoc, deleteDoc, where, writeBatch } = require('firebase/firestore');
const { describe, it, before, beforeEach, after } = require('mocha');

const projectId = 'demo-readventures-rules';
let env;
const material = (owner, extra = {}) => ({ title: 'Teacher Story', body: 'Body', createdBy: owner, isTeacherMade: true, ...extra });
const question = (owner, materialId, questionOrder = 0) => ({
  materialId, question: 'Question?', choices: ['A', 'B', 'C', 'D'],
  questionOrder, answerKeySeparated: true, createdBy: owner, isTeacherMade: true
});
const answerKey = (owner, materialId, correctIndex = 0) => ({ materialId, correctIndex, createdBy: owner });

before(async () => {
  env = await initializeTestEnvironment({ projectId, firestore: { rules: fs.readFileSync('firestore.rules', 'utf8') } });
});
beforeEach(async () => {
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async context => {
    const db = context.firestore();
    await setDoc(doc(db, 'users/teacherA'), { role: 'teacher' });
    await setDoc(doc(db, 'users/teacherB'), { role: 'teacher' });
    await setDoc(doc(db, 'users/studentA'), { role: 'student' });
    await setDoc(doc(db, 'users/invalidRole'), { role: 'teacher-pending' });
    await setDoc(doc(db, 'users/missingRole'), { displayName: 'No role' });
    await setDoc(doc(db, 'readingMaterials/sample'), { title: 'Sample', createdBy: 'sample', isTeacherMade: false });
    await setDoc(doc(db, 'readingMaterials/a-story'), material('teacherA'));
    await setDoc(doc(db, 'readingMaterials/b-story'), material('teacherB'));
    await setDoc(doc(db, 'questions/a-question'), question('teacherA', 'a-story'));
    await setDoc(doc(db, 'questions/key-question'), question('teacherA', 'a-story', 1));
    await setDoc(doc(db, 'questions/b-question'), question('teacherB', 'b-story'));
    await setDoc(doc(db, 'questions/legacy-question'), {
      materialId: 'a-story',
      question: 'Legacy?',
      choices: ['A', 'B', 'C', 'D'],
      correctIndex: 2,
      createdBy: 'teacherA',
      isTeacherMade: true
    });
    await setDoc(doc(db, 'scores/studentB-score'), { userId: 'studentB', score: 4 });
  });
});
after(async () => {
  if (env) await env.cleanup();
});

describe('ReadVentures Firestore rules', () => {
  const db = uid => env.authenticatedContext(uid).firestore();
  const anon = () => env.unauthenticatedContext().firestore();

  it('denies client role writes and self-elevation', async () => {
    await assertFails(setDoc(doc(db('studentA'), 'users/studentA'), { role: 'teacher' }));
    await assertFails(updateDoc(doc(db('studentA'), 'users/studentA'), { role: 'teacher' }));
    await assertFails(setDoc(doc(db('studentA'), 'users/teacherB'), { role: 'teacher' }));
  });
  it('allows registration profiles only for the authenticated UID with the student role', async () => {
    const newStudentDb = env.authenticatedContext('newStudent', {
      email: 'new.student@example.com'
    }).firestore();
    await assertSucceeds(setDoc(doc(newStudentDb, 'users/newStudent'), {
      name: 'New Student',
      email: 'new.student@example.com',
      role: 'student'
    }));
    await assertFails(setDoc(doc(newStudentDb, 'users/newTeacher'), {
      name: 'New Teacher',
      email: 'new.student@example.com',
      role: 'teacher'
    }));
    await assertSucceeds(updateDoc(doc(newStudentDb, 'users/newStudent'), {
      name: 'Updated Student',
      grade: '7A'
    }));
    await assertFails(setDoc(doc(newStudentDb, 'users/newStudent'), {
      name: 'New Student',
      email: 'new.student@example.com',
      role: 'teacher'
    }));
    await assertFails(updateDoc(doc(newStudentDb, 'users/newStudent'), { role: 'teacher' }));
  });
  it('recognizes teacher access only from the trusted teacher profile', async () => {
    await assertSucceeds(setDoc(doc(db('teacherA'), 'readingMaterials/trusted-teacher-story'), material('teacherA')));
    await assertFails(setDoc(doc(db('studentA'), 'readingMaterials/student-story'), material('studentA')));
    await assertFails(setDoc(doc(db('no-profile'), 'readingMaterials/no-profile-story'), material('no-profile')));
    await assertFails(setDoc(doc(db('invalidRole'), 'readingMaterials/invalid-role-story'), material('invalidRole')));
    await assertFails(setDoc(doc(db('missingRole'), 'readingMaterials/missing-role-story'), material('missingRole')));
  });
  it('allows authenticated students to read materials and denies anonymous reads', async () => {
    await assertSucceeds(getDoc(doc(db('studentA'), 'readingMaterials/a-story')));
    await assertFails(getDoc(doc(anon(), 'readingMaterials/a-story')));
  });
  it('allows a teacher to create an owned material', async () => {
    await assertSucceeds(setDoc(doc(db('teacherA'), 'readingMaterials/new-story'), material('teacherA')));
  });
  it('denies students from creating, updating, or deleting teacher materials', async () => {
    await assertFails(setDoc(doc(db('studentA'), 'readingMaterials/student-story'), material('studentA')));
    await assertFails(updateDoc(doc(db('studentA'), 'readingMaterials/a-story'), { title: 'No' }));
    await assertFails(deleteDoc(doc(db('studentA'), 'readingMaterials/a-story')));
  });
  it('denies another teacher from changing or deleting a teacher material', async () => {
    await assertFails(updateDoc(doc(db('teacherB'), 'readingMaterials/a-story'), { title: 'No' }));
    await assertFails(deleteDoc(doc(db('teacherB'), 'readingMaterials/a-story')));
  });
  it('denies deletion of built-in sample material', async () => {
    await assertFails(deleteDoc(doc(db('teacherA'), 'readingMaterials/sample')));
  });
  it('allows a teacher to delete their own teacher-made material', async () => {
    await assertSucceeds(deleteDoc(doc(db('teacherA'), 'readingMaterials/a-story')));
  });
  it('allows atomic material, question, and answer-key creation via getAfter', async () => {
    const teacherDb = db('teacherA'); const batch = writeBatch(teacherDb);
    batch.set(doc(teacherDb, 'readingMaterials/batch-story'), material('teacherA'));
    batch.set(doc(teacherDb, 'questions/batch-question'), question('teacherA', 'batch-story'));
    batch.set(doc(teacherDb, 'questionAnswerKeys/batch-question'), answerKey('teacherA', 'batch-story'));
    await assertSucceeds(batch.commit());
  });
  it('denies questions for another teacher material and edits to another teacher question', async () => {
    await assertSucceeds(setDoc(doc(db('teacherA'), 'questions/new-question'), question('teacherA', 'a-story')));
    await assertFails(setDoc(
      doc(db('teacherA'), 'questions/answer-leaking-question'),
      { ...question('teacherA', 'a-story'), correctIndex: 1 }
    ));
    await assertFails(setDoc(doc(db('teacherB'), 'questions/stolen'), question('teacherB', 'a-story')));
    await assertFails(updateDoc(doc(db('teacherB'), 'questions/a-question'), { question: 'No' }));
    await assertFails(deleteDoc(doc(db('teacherB'), 'questions/a-question')));
    await assertFails(updateDoc(doc(db('studentA'), 'questions/a-question'), { question: 'No' }));
  });
  it('allows students to read only prompt data from separated questions', async () => {
    const snapshot = await getDoc(doc(db('studentA'), 'questions/a-question'));
    if (snapshot.data().correctIndex !== undefined || snapshot.data().answerKey !== undefined) {
      throw new Error('Student-visible question exposed an answer field.');
    }
    const visibleQuestions = await getDocs(query(
      collection(db('studentA'), 'questions'),
      where('materialId', '==', 'a-story'),
      where('answerKeySeparated', '==', true)
    ));
    if (visibleQuestions.empty) throw new Error('Separated student questions were not queryable.');
    await assertFails(getDoc(doc(db('studentA'), 'questions/legacy-question')));
    await assertFails(getDoc(doc(anon(), 'questions/a-question')));
  });
  it('denies client score creation and modification while limiting reads to the owner', async () => {
    await assertFails(setDoc(doc(db('studentA'), 'scores/studentA-score'), { userId: 'studentA', score: 5 }));
    await assertFails(setDoc(doc(db('studentA'), 'scores/not-mine'), { userId: 'studentB', score: 5 }));
    await assertFails(updateDoc(doc(db('studentA'), 'scores/studentB-score'), { score: 1 }));
    await assertFails(updateDoc(doc(db('studentA'), 'scores/studentA-score'), { score: 1 }));
    await assertFails(getDoc(doc(db('studentA'), 'scores/studentB-score')));
  });
  it('restricts answer key reads and writes to the owning teacher', async () => {
    await assertFails(getDoc(doc(db('studentA'), 'questionAnswerKeys/a-question')));
    await assertFails(setDoc(
      doc(db('studentA'), 'questionAnswerKeys/a-question'),
      answerKey('studentA', 'a-story')
    ));
    await assertSucceeds(setDoc(
      doc(db('teacherA'), 'questionAnswerKeys/key-question'),
      answerKey('teacherA', 'a-story', 2)
    ));
    await assertFails(getDoc(doc(db('teacherB'), 'questionAnswerKeys/key-question')));
    await assertFails(setDoc(
      doc(db('teacherA'), 'questionAnswerKeys/b-question'),
      answerKey('teacherA', 'b-story')
    ));
  });
  it('allows dictionary reads but denies writes and denies unsupported class writes', async () => {
    await assertSucceeds(getDoc(doc(db('studentA'), 'dictionary_words/word')));
    await assertFails(getDoc(doc(anon(), 'dictionary_words/word')));
    await assertFails(setDoc(doc(db('studentA'), 'dictionary_words/word'), { word: 'x' }));
    await assertFails(setDoc(doc(db('teacherA'), 'classes/class1'), { className: 'Nope' }));
    await assertFails(setDoc(doc(db('teacherA'), 'class_members/member1'), { studentId: 'studentA' }));
    await assertFails(setDoc(doc(db('teacherA'), 'class_assignments/assignment1'), { title: 'Nope' }));
  });
});
