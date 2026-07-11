# Sourcely - AI Study Buddy

An Android app that turns your notes into an AI-powered study assistant. Add notes by typing or uploading PDFs, ask questions about them in a subject-specific chat, generate quizzes on demand, and track your quiz history over time.

Built in Java for Android (min SDK 26), using the Gemini API for AI features.

---

## Screenshots

<table>
  <tr>
    <td align="center"><b>Home</b></td>
    <td align="center"><b>AI Chat</b></td>
    <td align="center"><b>Add Notes (Text)</b></td>
    <td align="center"><b>Add Notes (PDF)</b></td>
  </tr>
  <tr>
    <td><img src="screenshots/home.jpg" width="180"/></td>
    <td><img src="screenshots/chat.jpg" width="180"/></td>
    <td><img src="screenshots/notes_text.jpg" width="180"/></td>
    <td><img src="screenshots/notes_pdf.jpg" width="180"/></td>
  </tr>
  <tr>
    <td align="center"><b>Quiz</b></td>
    <td align="center"><b>Answer Revealed</b></td>
    <td align="center"><b>Quiz Summary</b></td>
    <td align="center"><b>Quiz History</b></td>
  </tr>
  <tr>
    <td><img src="screenshots/quiz.jpg" width="180"/></td>
    <td><img src="screenshots/quiz_answer.jpg" width="180"/></td>
    <td><img src="screenshots/quiz_summary.jpg" width="180"/></td>
    <td><img src="screenshots/quiz_history.jpg" width="180"/></td>
  </tr>
</table>

---

## Features

**Subjects & organisation**
- Create subjects (displayed as colour-coded folder cards on the Home screen)
- Rename or delete subjects via a bottom sheet action menu
- Search subjects by name from the Home screen
- Folder cards show "Last quiz [date] · Best [N]%" for subjects with at least one quiz attempt

**Notes**
- Add text notes by typing or pasting directly
- Upload PDFs — text is extracted automatically and stored as a note
- View and edit text notes in a full-screen note viewer (PDF-derived notes are read-only to avoid diverging from the source)
- Delete notes with a confirmation dialog

**AI chat**
- Ask questions about your notes in a per-subject chat
- Answers are grounded strictly in your notes — if the answer isn't there, the AI says so
- Chat history persisted to Room; clear chat at any time
- Primary model: `gemini-2.5-flash`. Automatic fallback to `gemini-2.5-flash-lite` on a 429 (separate quota pool). Transient server errors (500/502/503/504) retry with backoff up to 2 times

**Quizzes**
- Generate a 5-question multiple choice quiz from any subject's notes
- One question at a time — tap an option to lock in your answer, correct/incorrect revealed on all options simultaneously, auto-advances after 1.5s
- Confetti celebration on scores ≥ 60%
- Exit mid-quiz triggers a confirmation dialog
- Retake a quiz without a new Gemini call (same question list re-used)
- Quiz summary shows score, percentage, and a per-question green/red recap strip

**Quiz history**
- Every completed attempt is saved to Room (`subjectId`, `score`, `total`, `timestamp`, missed question indices)
- View history per subject from the bottom sheet or the summary screen
- History screen shows each attempt's date, score, and a per-question correct/incorrect dot strip

**Dark mode**
- Manual toggle in Settings (persisted via `SharedPreferences`, applied via `AppCompatDelegate`)
- Full dark mode coverage across every screen — all colours use semantic resource references, no hardcoded hex

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java |
| Min SDK | 26 |
| Architecture | MVVM + Repository |
| Local database | Room (KSP) |
| Networking | Retrofit 2 + OkHttp + Gson |
| AI | Gemini API (`gemini-2.5-flash` / `gemini-2.5-flash-lite`) |
| Async | `ExecutorService` + `LiveData` |
| UI | XML layouts, `CardView`, `RecyclerView`, `MaterialToolbar` |
| Confetti | `nl.dionsegijn:konfetti-xml:2.0.5` |
| Build | Gradle Kotlin DSL, AGP 9.1.1 |

---

## Project structure

```
app/src/main/java/com/example/studybuddy/
├── data/
│   ├── db/             # Room DAOs (Subject, Note, Chat, QuizAttempt)
│   ├── model/          # Entities (Subject, Note, ChatMessage, QuizAttempt, QuizQuestion)
│   └── repository/     # Repositories wrapping DAOs + ExecutorService
├── network/            # Retrofit interface, Gemini request/response models
├── ui/
│   ├── BaseActivity    # Theme application + system-bar inset handling
│   ├── chat/           # ChatActivity, ChatViewModel, ChatAdapter
│   ├── home/           # HomeActivity, HomeViewModel, SubjectAdapter, SubjectActionsBottomSheet
│   ├── notes/          # AddNotesActivity, NoteViewerActivity, NoteAdapter, NotesViewModel
│   ├── quiz/           # QuizActivity, QuizSummaryActivity
│   │   └── history/    # QuizHistoryActivity, QuizAttemptAdapter
│   └── settings/       # SettingsActivity
└── utils/              # PdfExtractor, PromptBuilder, QuizGenerator, QuizParser, StyledDialog, ThemePreferences
```

---

## Setup

**1. Clone the repo**
```bash
git clone https://github.com/devashrutisangadi/ai-study-buddy.git
cd ai-study-buddy
```

**2. Add your Gemini API key**

Get a free API key from [Google AI Studio](https://aistudio.google.com/). Add it to your `local.properties` file (this file is gitignored and never committed):

```
GEMINI_API_KEY=your_key_here
```

The key is injected into the build via `BuildConfig.GEMINI_API_KEY` — see `app/build.gradle.kts` for the `buildConfigField` setup.

**3. Build and run**

Open in Android Studio, sync Gradle, and run on a device or emulator running API 26+.

> **Free tier note:** Google's free Gemini quota is stricter than their documentation implies and varies by account. If you hit rate limit errors frequently, check your actual limits in the [AI Studio dashboard](https://aistudio.google.com/) rather than relying on generic quota figures.

---

## Download

Grab the latest APK from the [Releases](../../releases/latest) page — no build required.

> The APK is a debug build signed with a dev key. On install Android may warn you about installing from unknown sources — this is expected for sideloaded APKs.

---

## Database

Room database: `study_buddy_db`, currently at schema version 2.

`fallbackToDestructiveMigration()` is in use — intentional for a dev build. Add explicit `Migration` objects before any production release if you need to preserve user data across schema changes.

---

## Key design decisions

**Missed quiz indices stored as a comma-separated String** (e.g. `"0,2,4"`) rather than using a Room `TypeConverter` for `List<Integer>`. Consistent with this project's no-converter convention — keeps the schema simple and the serialization logic explicit.

**Manual dark mode toggle** rather than following the system setting. The user opts in explicitly; the preference is saved to `SharedPreferences` and applied before `setContentView()` in `BaseActivity` to avoid a flash of the wrong theme.

**`StyledDialog` replaces all `AlertDialog.Builder` usage** app-wide — a custom `Dialog` subclass with confirm (danger/neutral) and input modes, consistent cream/purple styling, and explicit window sizing (88% screen width) to avoid the narrow-dialog rendering bug in plain `Dialog` windows.

**Gemini model fallback on 429** uses `gemini-2.5-flash-lite` (separate quota pool) rather than retrying the same model. Retrying an exhausted quota just burns more of it — the fallback only fires once before giving up with a user-facing message.

---

## Screens

| Screen | Description |
|---|---|
| Home | Subject folder grid, search, settings access, quiz progress on cards |
| Chat | Per-subject AI chat grounded in notes |
| Add Notes | Three-tab interface: type text, upload PDF, view saved notes |
| Note Viewer | Full-screen note view/edit (text) or read-only (PDF-derived) |
| Quiz | One-question-at-a-time interactive quiz |
| Quiz Summary | Score, confetti, per-question recap strip, history link |
| Quiz History | All attempts for a subject, newest first, with dot strips |
| Settings | Dark mode toggle |
