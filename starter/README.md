# ReadVentures — Student & Teacher Reading App (Capstone)

**ReadVentures** (Read + Adventures) — *"Read. Understand. Explore."*
A smart mobile application for improving reading and comprehension skills,
for Muntinlupa National High School.

**Stack:** Kotlin + Android Studio, Jetpack Compose (Kotlin 2.0.20, AGP 8.5.2, Gradle 8.7),
Firebase (Auth + Firestore), Material 3 Blue Theme (`#2563EB`).
Runs in **sample mode** (offline / on-device SharedPreferences via `ProgressStore`) when
Firebase is not yet configured.

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
- **Unlock Rule:** Finish all reading materials in a level (100% read) **AND** pass their quizzes with **>= 80%** (at least 4 out of 5 correct) to unlock the next level.
- **Gating UI:** Locked levels and stories are greyed out with lock icons. Tapping a locked story or level shows an alert with exact requirements to unlock ("Finish Level X first to unlock").
- **My Progress:** Displays current level status and what is needed to unlock the next level.

### 3. Class Code + Solo Mode vs Class Mode
- **Dual Roles:** Student (`role = "student"`) and Teacher (`role = "teacher"`).
- **Home Mode Switch:** Toggle between **Solo Mode** (personal reading journey) and **Class Mode** (enrolled class view with teacher assignments).
- **Demo Class:** **Grade 7 – Rizal**, Class Code: **`READ-7A1`**, Teacher: **Ms. Santos** (English 7, SY 2026-2027).
- **Assigned Readings:** Includes *The Power of Kindness* (Due: Tomorrow, 5:00 PM) and *Volcanoes* (Due: Friday, 5:00 PM).
- **Join Class:** Students can enter any 6-character code (e.g. `READ-7A1`). Wrong code displays an error; correct code enrolls the student.
- **Teacher Dashboard:** Teachers can view their classes, copy class codes with one tap, view the student roster and scores, assign library readings with custom due dates, and create new classes.

### 4. Teacher Upload / Create Own Quiz + Reading
- **Create Reading & Quiz in One Flow:** Teachers can write or paste a new reading material (title, category, level, topic, passage body) and add up to 5 multiple-choice questions with 4 options (A–D) and a designated correct answer.
- **Manage Materials:** Teachers can edit or delete their created materials and quizzes directly from the Teacher Dashboard.
- **Instant Student Availability:** Once saved, teacher-created materials appear in the Library and can be assigned to classes. Students take them with the same auto-check, instant feedback, and scoring system as standard materials.

---

## Demo Accounts & Class Code

| Account Type | Name | Email | Password | Role |
|--------------|------|-------|----------|------|
| **Demo Student** | Juan Dela Cruz | `juan.delacruz@mnhs.edu.ph` | `password123` | Student |
| **Demo Teacher** | Ms. Santos | `teacher.santos@mnhs.edu.ph` | `password123` | Teacher |

- **Demo Class Code:** `READ-7A1` (Grade 7 – Rizal, English 7)
- One-tap buttons for both demo accounts are available on the Login screen.

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
1. Open the app and log in as demo student (**Juan Dela Cruz**).
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

### 3. Test Class Mode & Join Class
1. On the **Home** screen, tap the **Class Mode** tab at the top.
2. If already enrolled in the demo class, you will see **Grade 7 – Rizal** (`READ-7A1`), teacher **Ms. Santos**, and the 2 assigned readings with due dates and progress bars.
3. To test joining with a code: tap **Join Another Class** (or log in with a new student account).
4. Enter an invalid code like `WRONG1` and tap **Join Class** → shows error: `"Invalid class code: WRONG1. Please check with your teacher."`
5. Enter `READ-7A1` and tap **Join Class** → successfully enrolls into Grade 7 – Rizal.

### 4. Test Teacher Dashboard & Quiz / Reading Creation
1. Log out from Profile and tap **Continue as demo teacher (Ms. Santos)** on the Login screen.
2. Notice the blue **Teacher Mode Active** banner on Home. Tap **Dashboard →** (or tap Profile → Teacher Dashboard).
3. In the Teacher Dashboard:
   - View your classes (`Grade 7 – Rizal`, code `READ-7A1`).
   - Tap the code or copy icon to copy it to the clipboard.
   - View the student roster (Juan Dela Cruz) with reading counts and average quiz scores.
   - Tap **"+ Assign Reading to Class"** to assign any library material with a due date.
4. Tap **"Upload Story"**:
   - Enter Title: `The Brave Carabao`
   - Category: `Fiction`, Level: `Level 1 (Grade 7)`, Topic: `Courage`
   - Body: `Once upon a time in Muntinlupa, a brave carabao helped farmers protect the harvest...`
   - Question 1: `What did the carabao protect?`
     - Choices: `The river`, `The harvest`, `The mountain`, `The village store`
     - Select choice B as correct answer.
   - Tap **"Save Story & Quiz"**.
5. Log back in as student Juan Dela Cruz:
   - Go to **Library**: *The Brave Carabao* appears in the library.
   - Open it, read it, and take the teacher-created quiz — it automatically checks the answer, provides instant feedback, and records the score!

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
          SampleData.kt          — 5 stories, 25 questions, 20 dictionary words, 3 levels, demo class & assignments
          AppState.kt            — shared state, unlock checks, class enrollment, teacher content management
          ProgressStore.kt       — SharedPreferences on-device persistence
          AuthRepository.kt      — Firebase Auth + sample-mode fallback (student & teacher)
          ReadingRepository.kt   — Firestore + sample-mode fallback
        ui/theme/
          Color.kt / Theme.kt    — ReadVentures blue theme (#2563EB)
        ui/components/
          AppChrome.kt           — LogoMark, AppTopBar, SubHeader, BottomNav
        ui/screens/
          LoginScreen.kt         — student & teacher registration/login + demo accounts
          HomeScreen.kt          — Solo/Class mode switch, categories, assignments, quick actions
          LibraryScreen.kt       — search, filters, level grouping, lock states
          ReadingScreen.kt       — story text with vocabulary highlighting & tap-to-define
          DictionaryScreen.kt    — searchable dictionary & glossary with story links
          LevelsScreen.kt        — 3 reading levels, unlock rules & progress
          MyClassScreen.kt       — student class view with assigned readings & due dates
          JoinClassScreen.kt     — join class via 6-character code
          TeacherDashboardScreen.kt — teacher class management, code copy, student roster, assigning readings
          CreateClassScreen.kt   — create class & generate unique code
          CreateMaterialQuizScreen.kt — teacher upload reading passage & 5-question quiz
          QuizScreen.kt          — quiz questions with instant feedback
          QuizResultScreen.kt    — score summary & retake
          ProgressScreen.kt      — reading stats, scores, current level & unlock requirement
          ProfileScreen.kt       — account details, learning navigation, logout
          EditProfileScreen.kt   — edit name and details
          ChangePasswordScreen.kt— change password with strength meter
```
