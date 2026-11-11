# ReadVentures — Student & Teacher Reading App (Capstone)

**ReadVentures** (Read + Adventures) — *"Read. Understand. Explore."*
A smart mobile application for improving reading and comprehension skills,
for Muntinlupa National High School.

**Stack:** Kotlin + Android Studio, Jetpack Compose (Kotlin 2.0.20, AGP 8.5.2, Gradle 8.7),
Firebase (Auth + Firestore + Cloud Functions), Material 3 Blue Theme (`#2563EB`).
Bundled reading stories, questions, dictionary entries, and reading progress remain available
locally. Authentication, user profiles, teacher-created materials, quiz scores, and password
operations require the configured Firebase backend; there is no local account mode.

---

## 4 New Core Features

### 1. Dictionary / Glossary
- **Vocabulary Words:** 20 Grade 7 vocabulary words mapped to stories (4 words per story), with definitions, parts of speech, and examples in sentences.
- **Dedicated Dictionary Screen:** Searchable list of words. Tap any word to view its definition, part of speech, sample sentence, and source story with a direct "Open Story" shortcut.
- **In-Reading Tap-to-Define:** Difficult words are highlighted in the story text with blue underlines. Tapping any highlighted word instantly opens a popup showing its meaning and example sentence without leaving the reading flow.
- Accessible from Home Quick Actions, Profile, and Reading screens.

### 2. Level Progression & Gating
- **3 Reading Levels:**
  - **Level 1 (Beginner):** Open by default. Stories: *The Power of Kindness*, *The Kind Farmer*.
  - **Level 2 (Intermediate):** Locked by default. Stories: *Volcanoes*, *Caring for Our Environment*.
  - **Level 3 (Advanced):** Locked by default. Story: *The Honest Woodcutter*.
- **Unlock Rule:** Finish all reading materials in a level (100% read) **AND** pass server-verified Firebase quizzes with **>= 80%** (at least 4 out of 5 correct) to unlock the next level. Bundled offline practice results are not persisted or used to unlock levels.
- **Gating UI:** Locked levels and stories are greyed out with lock icons. Tapping a locked story or level shows an alert with exact requirements to unlock ("Finish Level X first to unlock").
- **My Progress:** Displays current level status and what is needed to unlock the next level.

### 3. Class Navigation
- **Dual Roles:** Student (`role = "student"`) and Teacher (`role = "teacher"`).
- Student and teacher navigation remains available. Class creation, enrollment, rosters, and assignment persistence are not connected to Firestore; those screens report that limitation and do not claim to save data.

### 4. Teacher Upload / Create Own Quiz + Reading
- **Create Reading & Quiz in One Flow:** Teachers can write or paste a new reading material (title, category, level, topic, passage body) and add up to 5 multiple-choice questions with 4 options (A–D) and a designated correct answer.
- **Manage Materials:** Teachers can edit or delete their created materials and quizzes directly from the Teacher Dashboard.
- **Secure Student Grading:** Firebase-backed quizzes submit selected choices to the `submitQuiz` callable and show a result without revealing correct answers. Bundled practice quizzes can still be graded locally, but those practice results are not saved to Firebase or score history.

## Secure Firebase Quiz Grading

The original Firebase workflow loaded `correctIndex` from each public `questions` document, checked each answer in `QuizScreen`, and wrote the score from Android. That exposed answer keys and allowed clients to forge Firestore scores.

The updated Firebase workflow stores prompt-only documents in `questions/{questionId}` and the answer indexes in `questionAnswerKeys/{questionId}`. Teacher material, question, and answer-key writes are batched together. Firebase-authenticated students load only questions marked `answerKeySeparated: true`; they choose answers without per-question correctness feedback and call the authenticated `submitQuiz` Cloud Function. The function validates the complete answer set, grades it with the Admin SDK, and atomically saves an idempotent attempt and trusted score. Android displays local sample-quiz practice results without adding them to Firebase score history; only Cloud Function-confirmed scores appear in My Progress.

### Emulator checks

From `starter/`, with Node.js 22 and JDK 17 installed:

```powershell
npm run test:rules
npm run test:functions
.\gradlew.bat :app:assembleDebug
```

### Existing Firestore data migration and deployment

Do not assume legacy answer keys are protected until migration has completed. Back up the production database first. Deploy the restrictive rules so legacy questions are hidden from students, then use an Admin SDK identity with access to the project. The migration defaults to dry-run and never changes production data unless explicitly invoked:

```powershell
firebase deploy --only firestore:rules --project readventuresplmun
$env:GCLOUD_PROJECT = 'readventuresplmun'
node functions/scripts/migrate-answer-keys.js --dry-run
node functions/scripts/migrate-answer-keys.js --write-keys
node functions/scripts/migrate-answer-keys.js --dry-run
node functions/scripts/migrate-answer-keys.js --remove-legacy
npm --prefix functions run build
firebase deploy --only functions:submitQuiz --project readventuresplmun
```

`--write-keys` copies recognized legacy answer fields into `questionAnswerKeys` and leaves the public question documents untouched. Verify the reported counts and protected key documents before running `--remove-legacy`; that phase checks every question has a matching key before deleting legacy answer fields and marking prompts as separated. Malformed or ambiguous fields stop the migration for manual review. Deploying rules before migration temporarily hides old quizzes from students; new quizzes become available after the migration and callable deployment. No emulator data, real Firebase data, or functions are deployed by the test/build commands.

---

## Firebase Account Setup

- Add the Firebase project's `google-services.json` to `app/` locally (it is ignored by Git).
- Enable Email/Password in Firebase Authentication and configure a Firestore database.
- Deploy `firestore.rules` before allowing new registrations. Registration creates only a `student` profile at `users/{authUid}`.
- Provision teacher roles through a trusted administrator process; the app has no role selector and clients cannot change their role.
- Deploy the `submitQuiz` Cloud Function and migrate existing answer keys before using Firebase-backed quizzes.
- Real-account flows require a configured project and network connection; bundled sample reading material is not a demo account.

---

## Level Assignment Breakdown

| Level | Status | Required Stories | Unlock Condition |
|-------|--------|------------------|------------------|
| **Level 1: Beginner** | **OPEN** | *The Power of Kindness*, *The Kind Farmer* | Open by default for all students |
| **Level 2: Intermediate** | **LOCKED** | *Volcanoes*, *Caring for Our Environment* | Complete all Level 1 stories + score >= 4/5 (80%) on both quizzes |
| **Level 3: Advanced** | **LOCKED** | *The Honest Woodcutter* | Unlock Level 2, complete all Level 2 stories + score >= 4/5 (80%) on both quizzes |

---

## Step-by-Step Testing Guide

### 1. Test Dictionary / Glossary & In-Reading Tap-to-Define
1. Register a Firebase student account or sign in with an existing provisioned account.
2. On the **Home** screen, under *Quick Actions & Learning Tools*, tap **Dictionary**.
3. Type `"magma"` or `"ripple"` into the search bar. Tap the word to see its definition, part of speech, sample sentence, and source story.
4. Go back to Home and open the story **"The Power of Kindness"**.
5. Notice the blue underlined words (e.g., *struggling*, *errands*, *reward*, *ripple*).
6. Tap on **"struggling"** — a dialog immediately appears with:
   - Word: `struggling (verb)`
   - Meaning: `having difficulty doing something or putting in strong effort`
   - Example: `"Ana saw an old man struggling to carry a heavy basket."`
7. Tap **"Got it"** to dismiss the dialog and continue reading.

### 2. Test Level Progression & Gating
1. In the bottom navigation, tap **Library**.
2. Notice the stories in **Level 1** (*The Power of Kindness*, *The Kind Farmer*) have a book icon and can be opened.
3. Notice that **"The Honest Woodcutter"** (Level 3) is marked **LOCKED** with a lock icon.
4. Tap **"The Honest Woodcutter"** — a popup appears explaining:
   `Finish Level 2 first to unlock. Finish all Level 2 stories and pass their quizzes with 80%+ (4/5) to unlock.`
   The story cannot be opened.
5. Tap **Levels** from the Home quick actions to view the visual breakdown of all 3 levels, their lock states, and stories.
6. Now read *The Power of Kindness* and *The Kind Farmer*, mark each as finished, and score at least 4/5 on their quizzes.
7. Return to the Library: **Level 2** stories (*Volcanoes*, *Caring for Our Environment*) are now unlocked!

### 3. Class Management Availability
Class screens remain in student and teacher navigation, but class creation, joining, roster data, and assignments are not connected to Firestore. The app displays an unavailable message and does not create local success-shaped records.

### 4. Test Teacher Dashboard & Quiz / Reading Creation
1. Sign in with a Firebase account whose `users/{uid}.role` was provisioned as exactly `teacher` by a trusted administrator.
2. Tap **"Create reading"** on the Teacher Dashboard.
3. Enter a reading and quiz, then save. The app reports success only after the Firestore batch commits.
4. Edit or delete only materials owned by the signed-in teacher. Cover images continue to use Cloudinary.
5. To test a student flow, sign in with a Firebase student account:
   - Go to **Library**: the published material appears after it loads from Firestore.
   - Open it, read it, answer all questions, and submit. The app displays the server-generated result and records the trusted score without returning answer keys.
6. Class creation, roster, and assignment actions remain unavailable until their Firestore backend and rules are implemented.

---

## Project Structure
```
starter/
  settings.gradle.kts            — root project "ReadVentures", includes :app
  build.gradle.kts               — AGP 8.5.2, Kotlin 2.0.20, compose compiler plugin
  app/
    build.gradle.kts             — Jetpack Compose BOM 2024.09.00, Navigation 2.7.7, Firebase BOM 33.2.0
    src/main/
      AndroidManifest.xml        — MainActivity launcher, INTERNET permission
      java/com/mnhs/readventures/
        MainActivity.kt          — NavHost, routing for all screens
        data/
          Models.kt              — AppUser, ReadingMaterial, Question, Score, DictionaryWord, Level, SchoolClass, ClassMember, ClassAssignment
          FirebaseCollections.kt — collection names (users, readingMaterials, questions, scores, dictionary_words, classes, etc.)
          SampleData.kt          — 5 static stories, 25 questions, 20 dictionary words, 3 levels
          AppState.kt            — shared UI state and reading-level unlock checks
          ProgressStore.kt       — SharedPreferences reading-progress cache
          AuthRepository.kt      — Firebase Authentication and trusted Firestore profiles
          ReadingRepository.kt   — Firestore content, score, and secure quiz operations
        ui/theme/
          Color.kt / Theme.kt    — ReadVentures blue theme (#2563EB)
        ui/components/
          AppChrome.kt           — LogoMark, AppTopBar, SubHeader, BottomNav
        ui/screens/
          LoginScreen.kt         — Firebase student registration/login, password reset
          HomeScreen.kt          — reading dashboard, categories, class availability, quick actions
          LibraryScreen.kt       — search, filters, level grouping, lock states
          ReadingScreen.kt       — story text with vocabulary highlighting & tap-to-define
          DictionaryScreen.kt    — searchable dictionary & glossary with story links
          LevelsScreen.kt        — 3 reading levels, unlock rules & progress
          MyClassScreen.kt       — student class navigation and backend availability
          JoinClassScreen.kt     — honest unavailable state until class storage is implemented
          TeacherDashboardScreen.kt — teacher material management and class availability
          CreateClassScreen.kt   — honest unavailable state until class storage is implemented
          CreateMaterialQuizScreen.kt — teacher upload reading passage & 5-question quiz
          QuizScreen.kt          — local demo grading and server-submitted Firebase quizzes
          QuizResultScreen.kt    — score summary & retake
          ProgressScreen.kt      — reading stats, scores, current level & unlock requirement
          ProfileScreen.kt       — account details, learning navigation, logout
          EditProfileScreen.kt   — edit name and details
          ChangePasswordScreen.kt— change password with strength meter
  functions/
    src/index.ts                    — authenticated submitQuiz callable
    scripts/migrate-answer-keys.js  — dry-run-first Admin SDK migration
```
