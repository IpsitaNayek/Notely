# Claude Code Prompt — Notely: Minimal Offline-First Notes App (Android)

You are implementing **Notely**, a minimalist note-taking Android app in **Kotlin + Jetpack Compose**, with:

- **Offline notes** stored in **Room**
- **Online sync** through **Firebase** (Auth + Cloud Firestore)
- **Hilt** for dependency injection
- **MVVM** architecture (unidirectional data flow)
- A **soft, glassy, warm-earth-toned minimal theme** with big light-weight typography (see §10 and the reference image)
- The **smallest possible number of screens**

The app must feel fast, quiet and distraction-free. Writing a note should take one tap and zero decisions.

---

## 0. Critical instruction: inspect the project first

Before writing or changing any code:

1. Inspect the existing repository (if any): Gradle files (`build.gradle.kts`, `libs.versions.toml`, `settings.gradle.kts`), `AndroidManifest.xml`, package name / application ID, compile/target/min SDK, Kotlin version, Compose BOM version, existing resources and architecture.
2. Check whether `google-services.json` exists in `app/`.
   - If it does not, **do not fabricate one**. Build everything else, make the app compile and run fully offline, keep Firebase code behind interfaces, and clearly tell me at the end which Firebase Console steps I still need to do (see §14).
3. Make a short implementation plan (10–15 lines) before coding.
4. Reuse existing code where it is sound. Do not rewrite a working project unnecessarily.

### Visual goal

Do **not** build a generic Material "notes app".

Build the look of the supplied reference image (`docs/design/notely-reference.jpg`): a **soft, glassy, warm-earth-toned** UI with a **huge light-weight "Your Notes" title**, **pill filter chips**, a **white featured card**, **muted colored note cards** in a two-column grid, and a **floating dock + FAB**. **§10 is the authoritative visual spec.** Where the reference and the rest of this document disagree, §10 wins for visuals and §2 wins for the number of screens.

---

# 1. Product requirements

## 1.1 Core behaviour

1. Create, edit, delete and restore notes.
2. Each note has a **title** (optional) and a **body**.
3. Notes are **always saved locally first** (Room is the single source of truth).
4. Notes can be **Local-only** or **Synced**:
   - **Local-only**: never leaves the device.
   - **Synced**: mirrored to Firestore under the signed-in user's account and available on their other devices.
5. The app is **fully usable without an account and without internet**.
6. Signing in is **optional** and only needed for synced notes.
7. Search notes by title/body (case-insensitive, substring).
8. Pin notes to the top.
9. Deleted notes go to a soft-deleted state first (undo via Snackbar), and are permanently removed after sync has propagated the deletion (or immediately, if the note was local-only).

## 1.2 Deliberately out of scope

Do **not** add unless I ask later: folders/labels, rich text/markdown rendering, images/attachments, reminders, widgets, biometric lock, multi-window drag & drop, export/import.

Keep the scope small. Quality of the core loop matters more than feature count.

---

# 2. Screens — maximum two destinations

The app must have **only two real screens**. Anything else is an overlay (bottom sheet / menu / dialog), not a page.

| # | Screen | Purpose |
|---|--------|---------|
| 1 | **Notes screen** (home) | Header, filter chips, featured pinned note, colored note grid, search, sync status |
| 2 | **Editor screen** | Create/edit a single note |

Everything else is an overlay on Screen 1:

- **Account & sync sheet** (opened from the floating dock's Account item): sign in/out, sync-now, last-synced time, default note type (Local/Synced), theme override (System/Light/Dark).
- **⋮ overflow menu** (top-right): Sort (Recently edited / Created / Title) and Sync now.
- **Per-note ⋮ menu** (on every card): Pin/Unpin, Color, Switch Local/Synced, Delete.
- **Trash** is **not** a screen: deleting shows an "Undo" snackbar.
- **Search** is **not** a screen: it is an inline field that expands at the top of the Notes screen.

Do not add: onboarding, splash screens beyond the system splash, a settings page, a login page, an about page, or a multi-destination bottom navigation bar.

> **Reference note:** the reference image's floating dock has three tabs (notes, calendar, profile). In Notely the dock has **two items only**: *Notes* (the current screen) and *Account* (opens the bottom sheet). It is a visual element, not navigation. There is no calendar page.

Navigation: Navigation-Compose (or a simple type-safe route setup) with exactly two routes: `notes` and `editor?noteId={id}`.
---

# 3. Notes screen (Screen 1) — behaviour

Layout (modelled on the reference image; exact styling is in §10):

```text
[status bar]                                   ( ⌕ )  ( ⋮ )

                       Your
                       Notes

 [ All ] [ Pinned ] [ Synced ] [ Local ]  →

 ┌─────────────────────────────────────────┐
 │ 📌 15 Oct                            ⋮  │   featured white card =
 │ ✔ Work Summary                          │   most recently edited
 │ Project handoff completed; awaiting…    │   pinned note
 └─────────────────────────────────────────┘
    ╲  ghost layers (other pinned notes)  ╱

 ┌────────────────────┐ ┌────────────────────┐
 │ 16 Oct          ⋮  │ │ 17 Oct          ⋮  │   2-column grid of
 │ All Account        │ │ Travel Plans       │   colored note cards
 │ Book hotel in …    │ │ Key takeaways …    │
 └────────────────────┘ └────────────────────┘

   ┌───────────────────────┐   ┌──────┐
   │  [▮ notes]     (👤)   │   │  ＋  │        floating dock + FAB
   └───────────────────────┘   └──────┘
```

## Header

- Two **circular glass icon buttons** pinned top-right: **Search** and **⋮ overflow** (Sort, Sync now). They stay fixed while the content scrolls.
- Below them, the large, centered display title **"Your Notes"** on two lines ("Your" / "Notes"), in the Light weight (see §10.5). The title scrolls away with the content.
- The name "Notely" appears only as the launcher label and system splash — not as an in-app wordmark.

## Filter chips

- Horizontally scrollable row: **All · Pinned · Synced · Local**.
- Selected chip = accent-filled pill; unselected = glass pill (see §10.7). Let the last chip be cut off by the screen edge to hint that the row scrolls.
- Filters are mutually exclusive and live in `NotesUiState.filter`.
- The reference shows "Simple Notes" and "To-Do" chips. These are **intentionally replaced** with Synced/Local because checklist notes are out of scope (§1.2) and Synced/Local matches Notely's sync model.

## Featured card

- When the filter is **All** and at least one note is pinned, show the **most recently edited pinned note** as a white featured card: pin glyph + date, sync-status glyph, bold title, one-line preview with ellipsis, ⋮ menu.
- If **two or more** notes are pinned, draw up to **two ghost layers** behind the card (offset ≈ 8 dp and 16 dp downward, ±2° rotation, translucent glass, faded preview text). They are decorative only and hint that more pinned notes exist.
- The other pinned notes appear **first in the grid**, with a small pin glyph. The featured note is not repeated in the grid.
- With the **Pinned** filter active, hide the featured card and show all pinned notes in the grid.

## Note grid

- Two-column staggered grid (`LazyVerticalStaggeredGrid`), 12 dp gutters, 16 dp horizontal padding. Header, chips and featured card are `FullLine` span items so everything scrolls together.
- Each card uses the **note's own color** (§10.4). Top row: date, sync glyph, ⋮. Then a bold title (max 2 lines) and a preview (max 4 lines). Height follows content within a min/max range so the grid looks tidy.
- Sync glyph (also used on the featured card):
  - Synced → filled accent check-circle
  - Local-only → hollow circle
  - Pending upload → small progress ring
  - Sync error → warning glyph (tap opens the Account & sync sheet)

## Interactions

- **Tap** a card → open Editor.
- **Tap ⋮** or **long-press** a card → menu: Pin/Unpin, Color, Switch to Local/Synced, Delete.
- **No swipe gestures** (they don't suit a grid). Delete shows a floating "Note deleted · Undo" snackbar above the dock.
- **Search**: tapping the search button collapses the header and expands an inline glass search field with a cancel action. Results filter live from Room (debounced ~200 ms). Chips and the featured card are hidden while searching. Back closes search first.
- **Empty state**: below the header, one quiet line — "Nothing here yet." with a smaller "Tap ＋ to write." No illustrations. For an empty filter/search result: "No notes match."

## Floating dock + FAB

- **Dock**: a floating pill with two items — *Notes* (selected; tap scrolls to top) and *Account* (opens the Account & sync sheet; shows a tiny accent dot when there is a sync error, or when synced notes are pending while signed out).
- **FAB**: a rounded-square button with an accent "＋", placed to the right of the dock on the same row. It opens the Editor with a new note.
- Content scrolls **beneath** both. Give the grid bottom padding of at least 104 dp plus the navigation-bar inset, and add a soft bottom fade so cards don't collide with the dock.

Use stable keys (`note.id`) in the grid, and `animateItem()` for gentle reorder/insert animations where supported.
---

# 4. Editor screen (Screen 2) — behaviour

```text
 ( ← )                            [ ☁ Synced ]  ( ⋮ )

 Title
 16 Oct · Edited 12:41

 Body text starts here and grows…



 [ ● ● ● ● ● ● ]  ( 📌 )        ← floating glass pill above the keyboard
```

## Visuals

- The editor's **full-screen background is the note's own color** (§10.4), with the same subtle grain as the rest of the app. Changing the color crossfades over ~200 ms. This links a card in the grid to its editor.
- Top row: circular glass buttons for **Back** and **⋮** (Pin, Delete, Share as plain text), and a glass pill showing the sync mode (**Local** / **Synced**) that toggles on tap.
- **Title**: 32 sp Light (echoing the "Your Notes" header). **Body**: 17 sp Regular, line-height ≈ 1.5. Text is white on the note color. Meta line ("16 Oct · Edited 12:41") is 12 sp, tertiary color.
- Bottom: one floating glass pill above the IME containing the **six color dots** (§10.4) and a **pin** toggle. Nothing else.

## Behaviour

- **Autosave**: debounced (~500 ms) writes to Room while typing, plus a guaranteed save on back/stop/`onCleared`. **No Save button.**
- New note: keyboard opens with focus on the **title** (decide and document if you prefer the body).
- If the user leaves a new note completely empty, **do not create it**.
- Back button and navigate-up behave identically.
- Handle process death: draft state survives via `SavedStateHandle` and the Room autosave.
- New notes get a default `colorId` derived deterministically from the note id, so the grid looks varied without asking the user to choose.
- Switching a note from Local → Synced while signed out lets the user keep editing; on return to the Notes screen, open the Account & sync sheet.
- Optional polish (only if stable): a shared-element / container transform from the tapped card into the editor. Otherwise use a ~220 ms fade with a very slight scale (0.98 → 1).
---

# 5. Architecture

Use **MVVM + Repository**, single-activity, unidirectional data flow. Do not over-engineer: **no separate Gradle modules, no use-case class per method**. Add a domain layer only where it removes real duplication.

Suggested structure (adapt if you have a better one, but keep responsibilities separated):

```text
app/src/main/java/<package>/
  NotelyApp.kt                       // @HiltAndroidApp, WorkManager config
  MainActivity.kt                    // @AndroidEntryPoint, single activity

  di/
    DatabaseModule.kt                // Room DB + DAO
    FirebaseModule.kt                // FirebaseAuth, FirebaseFirestore
    RepositoryModule.kt              // @Binds interfaces -> impls
    DispatchersModule.kt             // @IoDispatcher etc.

  data/
    local/
      NotelyDatabase.kt
      NoteDao.kt
      NoteEntity.kt
      Converters.kt (only if needed)
      PreferencesDataStore.kt        // theme, default note type, last sync time
    remote/
      NoteRemoteDataSource.kt        // interface
      FirestoreNoteRemoteDataSource.kt
      NoteDto.kt
      AuthDataSource.kt              // interface
      FirebaseAuthDataSource.kt
    sync/
      SyncWorker.kt                  // @HiltWorker
      SyncScheduler.kt               // enqueue unique work
      SyncEngine.kt                  // push/pull/merge logic (pure-ish, testable)
    repository/
      NoteRepository.kt              // interface
      NoteRepositoryImpl.kt
      AuthRepository.kt
      SettingsRepository.kt
    mapper/
      NoteMappers.kt                 // Entity <-> Domain <-> Dto

  domain/
    model/
      Note.kt
      SyncMode.kt                    // LOCAL_ONLY, SYNCED
      SyncState.kt                   // SYNCED, PENDING, ERROR, NOT_APPLICABLE
      AuthState.kt

  ui/
    theme/
      Color.kt, Type.kt, Shape.kt, Theme.kt
    navigation/
      NotelyNavHost.kt
    notes/
      NotesScreen.kt
      NotesViewModel.kt
      NotesUiState.kt
      NoteRow.kt
      AccountSyncSheet.kt
    editor/
      EditorScreen.kt
      EditorViewModel.kt
      EditorUiState.kt
    components/
      (small shared composables only)
```

Rules:

- **Repository owns data.** ViewModels never touch DAO/Firestore/Auth directly.
- **ViewModels own UI state** (`StateFlow<UiState>`, one immutable state object per screen) and one-shot events (`SharedFlow`/`Channel`) for Snackbars/navigation.
- **Composables render state** and emit events. They contain no business logic.
- Use `collectAsStateWithLifecycle()`.
- Use `hiltViewModel()` for ViewModels.
- Inject dispatchers; do not hard-code `Dispatchers.IO` inside classes.
- Interfaces for `NoteRepository`, `NoteRemoteDataSource`, `AuthDataSource` so they can be faked in tests.

---

# 6. Hilt setup

- `@HiltAndroidApp` on the `Application`.
- `@AndroidEntryPoint` on `MainActivity`.
- `@HiltViewModel` + constructor injection for all ViewModels.
- `@Module @InstallIn(SingletonComponent::class)` modules for Room, Firebase, DataStore, repositories, dispatchers.
- Use `@Binds` for interface→implementation and `@Provides` only for third-party objects.
- Workers use `@HiltWorker` + `@AssistedInject` with `androidx.hilt:hilt-work`, and `NotelyApp` implements `Configuration.Provider` with `HiltWorkerFactory` (disable the default WorkManager initializer in the manifest).
- Use **KSP** (not kapt) for Hilt and Room if the project's Kotlin version supports it.
- Do not use the Service Locator pattern, manual singletons, or `object` holders for dependencies.

---

# 7. Data model

## 7.1 Room entity

```kotlin
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,          // UUID generated on device
    val title: String,
    val body: String,
    val isPinned: Boolean,
    val colorId: Int,                    // index 0..5 into the note color palette (§10.4)
    val syncMode: SyncMode,              // LOCAL_ONLY or SYNCED
    val syncState: SyncState,            // PENDING, SYNCED, ERROR, NOT_APPLICABLE
    val isDeleted: Boolean,              // soft delete / tombstone
    val createdAt: Long,                 // epoch millis
    val updatedAt: Long,                 // epoch millis, set on every local edit
    val remoteUpdatedAt: Long?           // last known server timestamp, if any
)
```

Rules:

- **Primary key is a client-generated UUID** (not an auto-increment Int) so the same note has the same id on Room and Firestore and across devices.
- Add indices on `updatedAt`, `isDeleted`, `syncState` where useful.
- Provide Room type converters for enums (or store them as strings) — do not rely on ordinals.
- Use a **versioned database with `exportSchema = true`** and write at least a placeholder migration strategy. Do not use `fallbackToDestructiveMigration()` in release code.

## 7.2 DAO

Use `Flow` for reads and `suspend` for writes. Provide at least:

- `observeActiveNotes(): Flow<List<NoteEntity>>` (not deleted, pinned first, then `updatedAt DESC`)
- `search(query: String): Flow<List<NoteEntity>>` (`LIKE` with proper escaping)
- `getById(id): NoteEntity?` / `observeById(id): Flow<NoteEntity?>`
- `upsert(note)`
- `getPendingSync(): List<NoteEntity>`
- `markSynced(id, remoteUpdatedAt)`
- `hardDelete(id)`
- `getAllSyncedIds()` (for reconciliation)

## 7.3 Firestore layout

```text
users/{uid}/notes/{noteId}
  title: String
  body: String
  isPinned: Boolean
  colorId: Int
  isDeleted: Boolean
  createdAt: Timestamp/Long
  updatedAt: Long (client time)
  serverUpdatedAt: Timestamp   // FieldValue.serverTimestamp()
```

- Notes are stored only under the signed-in user's path.
- Local-only notes are **never** written to Firestore.
- Provide **Firestore Security Rules** in a separate file (see §14) restricting read/write to `request.auth.uid == uid`, and validating field types/sizes.

---

# 8. Offline-first sync design

**Room is the source of truth. The UI reads only from Room.** Firestore is a replication target, never read directly by the UI.

## 8.1 Write path

1. User edits a note → write to Room immediately (`updatedAt = now`).
2. If `syncMode == SYNCED`: set `syncState = PENDING` and call `SyncScheduler.requestSync()`.
3. If `syncMode == LOCAL_ONLY`: `syncState = NOT_APPLICABLE`; nothing else happens.

## 8.2 Push

- `SyncWorker` (WorkManager, unique work name, `NetworkType.CONNECTED`, exponential backoff) pushes all `PENDING` notes for the signed-in user.
- Use `set(..., SetOptions.merge())` or a batched write; include `FieldValue.serverTimestamp()`.
- Deletions push `isDeleted = true` (tombstone). After the tombstone is acknowledged, hard-delete locally.
- Batch writes (max 500 ops per batch) and handle partial failure.
- On success → `markSynced`. On failure → keep `PENDING`, retry with backoff. After repeated hard failures (e.g. permission denied) → `ERROR`.

## 8.3 Pull

- While the app is in the foreground and the user is signed in, attach a Firestore **snapshot listener** on `users/{uid}/notes` (as a `callbackFlow`) and merge changes into Room.
- Also run a pull inside the Worker (for background/first sync).
- Do **not** re-push what was just pulled (avoid sync loops): merged remote changes are written with `syncState = SYNCED`.

## 8.4 Conflict resolution

- Default policy: **last-write-wins by `updatedAt`**, with the server timestamp as the tie-breaker.
- If the local copy is `PENDING` and the remote copy is newer, keep the newer one but **do not silently lose text**: save the losing version as a new note titled `"<original title> (conflict copy)"`. This is rare, but it must not destroy user data.
- Put the merge logic in a **pure, unit-testable** function (`SyncEngine.merge(local, remote)`).

## 8.5 Switching modes and accounts

- **Local → Synced**: mark `PENDING`; requires sign-in — if signed out, keep the note `SYNCED` intent but show `PENDING` and prompt to sign in; nothing is uploaded until then.
- **Synced → Local**: stop syncing this note; ask (in a dialog) whether to also delete the cloud copy (default: keep it out of sync and delete the remote copy).
- **Sign out**: keep local data; stop the listener; cancel pending sync work; synced notes remain on the device and become `PENDING` until signed in again.
- **Sign in as a different account**: do **not** upload the previous account's synced notes into the new account without an explicit confirmation dialog.

## 8.6 Sync triggers

- After each local edit to a `SYNCED` note (debounced).
- On app foreground (`ProcessLifecycleOwner`) if signed in.
- On network regained (WorkManager constraint handles this).
- Manual "Sync now" in the account sheet.
- Optionally one periodic worker (e.g. every 6–12 h, network-constrained). Do not over-schedule.

---

# 9. Authentication

Keep auth as simple as possible, and **optional**.

- Use **Firebase Auth** with **Google Sign-In through Credential Manager** (`androidx.credentials` + Google ID token → `GoogleAuthProvider.getCredential`). Do **not** use the deprecated `GoogleSignInClient` flow.
- Sign-in lives **only** in the account & sync bottom sheet (no login screen).
- Expose `AuthState` (`SignedOut`, `SignedIn(uid, email, displayName)`, `Loading`, `Error`) as a `StateFlow` from `AuthRepository`.
- Handle: user cancelled, no network, no Google account available, token errors.
- Never store credentials manually.

If email/password or anonymous auth is simpler for your setup, tell me before switching. Do not add multiple providers by default.

---

# 10. Visual design — theme & typography (from the reference image)

**Reference:** `docs/design/notely-reference.jpg` (a phone mockup titled "Your Notes"). If the file is not in the repo, ask me to add it. **Inspect it before writing any UI code.**

Read the reference correctly:

- The image is a **mockup on a presentation backdrop** (a terracotta → grey → teal gradient with grain). The *app screen itself* is the frosted, mid-dark phone panel in the middle. Design the app from that panel, not from the backdrop.
- The image is small and low-resolution. **All measurements and colors below are estimates sampled by eye.** Build a first version from them, then compare it against the reference side by side and tune.
- Where this section and the reference disagree on **accessibility** (text contrast), accessibility wins (see §17).

## 10.1 Design language in one paragraph

A **soft, glassy, warm-earth-toned** interface. The screen is a mid-dark taupe/graphite surface with a very faint warm-to-cool tint and visible film grain. Everything is **rounded**: pill chips, pill dock, large-radius cards. A huge, **light-weight** display title anchors the top of the screen; the rest of the typography is small, clean and quiet. Note cards are **muted earthy colors** (terracotta, sage, dusty blue…), which are the only real color on screen besides a single **coral accent**. Depth comes from **translucency, overlap and contrast** (a bright white featured card and a bright white dock on a dark surface), **not from heavy shadows**.

Principles:

- One accent (coral). Everything else is neutral or a muted note color.
- Big type contrast: very large Light display title vs. small Regular/Bold text.
- Generous rounded corners and breathing room.
- Translucent "glass" surfaces with a hairline light border instead of elevation.
- No pure black backgrounds, no neon, no Material purple, no dynamic color.

## 10.2 Background (the "screen")

Build a reusable `NotelyBackground` composable used by both screens (the Editor overrides it with the note color):

1. **Vertical gradient**, three stops (dark theme):
   - top: warm taupe `#6B5B54`
   - middle: neutral graphite `#5A5654`
   - bottom: cool slate `#4E5A5E`

   It must read as a **mid-dark taupe/graphite, not black**, with only a slight warm-to-cool shift.
2. **Film grain**: a small tileable monochrome noise texture (128–256 px) drawn tiled over the gradient at **≈ 4–7 % alpha**. Create/cache it **once per process** (e.g. `drawWithCache` + a `BitmapShader` with `REPEAT`); never regenerate it per frame or per recomposition.
3. Content scrolls over it. Add a soft bottom fade behind the dock.

Light theme (derived, since the reference shows only dark): gradient `#F1E9E3` → `#E9E5E1` → `#DDE4E6`, same grain at ≈ 3–5 %.

## 10.3 Color tokens

Expose tokens through a `NotelyColors` class provided via `CompositionLocal` (and mapped onto `MaterialTheme.colorScheme` only where needed for Material components). **No hard-coded colors inside composables.**

| Token | Dark (reference) | Light (derived) |
|---|---|---|
| `bgTop / bgMid / bgBottom` | `#6B5B54 / #5A5654 / #4E5A5E` | `#F1E9E3 / #E9E5E1 / #DDE4E6` |
| `textPrimary` | `#FFFFFF` | `#1F1B19` |
| `textSecondary` | white 72 % | `#1F1B19` at 66 % |
| `textTertiary` | white 50 % | `#1F1B19` at 45 % |
| `glassFill` | white 10 % | black 5 % |
| `glassBorder` | white 16 % (1 dp) | black 8 % (1 dp) |
| `accent` (coral) | `#D9513A` | `#D9513A` |
| `onAccent` | `#FFFFFF` | `#FFFFFF` |
| `featuredCard` | `#F6F3EF` | `#FFFFFF` |
| `onFeaturedCard` / secondary | `#1B1917` / `#6F6A66` | same |
| `dock` | `#F6F3EF` | `#26221F` |
| `onDock` | `#1B1917` | `#FFFFFF` |
| `dockSelectedItem` (bg) | `#26221F` (icon white) | white 16 % (icon white) |
| `fabContainer` | `#26221F` | `#26221F` |
| `fabIcon` | `accent` | `accent` |
| `danger` | `#F0776B` | `#C8402F` |

Accent (coral) is used **sparingly**: the selected chip, the FAB's plus, the synced check-circle, the primary "Sign in" button, and small status dots. Text selection handles/highlight use white at 25 % on colored surfaces.

**Dark is the reference. Tune the dark theme first**, then adjust the light theme so it feels like the same product (same shapes, same type, same accent, same note colors).

## 10.4 Note card colors

Each note has a `colorId` (index 0–5) stored in Room and synced to Firestore. Palette (starting values, darkened slightly from the reference so white text passes contrast):

| Id | Name | Hex |
|---|---|---|
| 0 | Terracotta | `#A5684F` |
| 1 | Sage | `#6F7C5F` |
| 2 | Dusty blue | `#5F7A86` |
| 3 | Sand | `#8A7654` |
| 4 | Clay rose | `#A06A6A` |
| 5 | Plum taupe | `#7D6577` |

- Text on colored cards: title white (100 %), preview white (75 %), meta white (65 %).
- **Verify contrast**: white text on every card color must reach **≥ 4.5:1** for body text. If a color fails, darken it slightly (keep hue and muted character).
- The same palette is used for the six color dots in the editor and for the Editor background.
- Default `colorId` for a new note = deterministic function of the note id (e.g. `abs(id.hashCode()) % 6`).
- The reference's featured card is **white**, not colored; it is reserved for the featured pinned note.

## 10.5 Typography

The reference uses a clean **geometric sans-serif** that looks like **DM Sans** (very light weight for the big title, bold for card titles). Use **DM Sans** for everything (free, SIL Open Font License, so it may be bundled).

- Bundle **static TTFs** in `res/font/`: `dm_sans_light.ttf` (300), `dm_sans_regular.ttf` (400), `dm_sans_medium.ttf` (500), `dm_sans_bold.ttf` (700). Bundle rather than using the downloadable-fonts provider so the offline-first app never depends on the network for its typeface.
- If you cannot fetch the font files in your environment, wire the `FontFamily` to the expected resource names, fall back to `FontFamily.SansSerif` in the meantime, and tell me exactly which files to add and where.

Type scale (sizes are estimates from the reference; tune by eye):

| Role | Weight | Size | Line height | Letter spacing | Used for |
|---|---|---|---|---|---|
| `display` | Light 300 | ≈ 56 sp | ≈ 1.0 | −0.5 sp | "Your Notes" (two lines, centered) |
| `editorTitle` | Light 300 | 32 sp | 1.15 | −0.2 sp | Note title in the Editor |
| `cardTitle` | Bold 700 | 17 sp (featured: 18 sp) | 1.2 | 0 | Titles on cards |
| `body` | Regular 400 | 17 sp | 1.5 | 0 | Editor body |
| `preview` | Regular 400 | 13 sp | 1.35 | 0 | Card previews |
| `label` | Medium 500 | 13 sp | — | +0.1 sp | Chips, buttons, menu items |
| `meta` | Regular 400 | 11–12 sp | — | +0.2 sp | Dates ("15 Oct"), "Edited 12:41" |

Rules:

- Light (300) is **only** for large text (≥ 28 sp). Never use it for small text.
- Sentence case everywhere; no all-caps, no underlines.
- Dates use a short, locale-aware "day + short month" format (e.g. "15 Oct"), not a hard-coded pattern.
- Use `sp` and respect the user's font scale; titles wrap, cards grow — nothing may clip at 1.3× font scale.

## 10.6 Shape & spacing

| Element | Radius |
|---|---|
| Note cards, featured card | 20–22 dp |
| Chips, dock, search field, glass pills, snackbar | fully rounded (pill) |
| Circular icon buttons | 40 dp circle |
| FAB | 64 dp rounded square, 20 dp radius |
| Dock selected item | 48 dp rounded square, 16 dp radius |
| Bottom sheet (top corners) | 28 dp |

Spacing: screen horizontal padding 16 dp; grid gutter 12 dp; card inner padding 14–16 dp; chip height 36 dp with 16 dp horizontal padding and 8 dp gaps; dock height 64 dp, floating 16 dp above the navigation-bar inset; header: 24 dp between the icon buttons, the title and the chip row.

Use a small `NotelySpacing` / `NotelyShapes` object exposed via `CompositionLocal` instead of scattering numbers.

## 10.7 Components

Put reusable pieces in `ui/components/`: `NotelyBackground`, `GlassIconButton`, `GlassPill`, `FilterChipRow`, `FeaturedNoteCard`, `NoteCard`, `SyncGlyph`, `FloatingDock`, `NotelyFab`, `ColorDotRow`, and a `Modifier.glass(shape)` helper.

- **Glass icon button**: 40 dp circle, `glassFill` + 1 dp `glassBorder`, thin outlined 20 dp icon in `textPrimary`.
- **Filter chip**: height 36 dp, pill. Selected = solid `accent` fill with `onAccent` text; unselected = glass with `textPrimary` at ≈ 85 %. `label` type. 150 ms color crossfade on change.
- **Featured card**: `featuredCard` background, 20 dp radius, 16 dp padding. Top row: small pin glyph (muted blue-grey `#7A8CA0`) + date on the left, ⋮ on the right. Below: `SyncGlyph` + bold title, then a one-line preview with ellipsis. Very soft, diffuse shadow only on this card. Ghost layers behind it: `glassFill` at slightly higher alpha, offset/rotated as in §3.
- **Note card**: solid note color (§10.4), 20 dp radius, no shadow, no border. Top row: date (`meta`, white 65 %), `SyncGlyph`, ⋮. Title `cardTitle` (2 lines max), preview `preview` (4 lines max).
- **Floating dock**: pill, `dock` background, two 48 dp items; the selected item sits in a `dockSelectedItem` rounded square with a white icon. Unselected icons use `onDock` at ≈ 60 %.
- **FAB**: `fabContainer` rounded square with an `accent` plus icon, same height as the dock, 12 dp gap from the dock.
- **Bottom sheet (Account & sync)**: dark glass surface (`#26221F` at ≈ 92 %; in light theme a light surface at ≈ 94 %), 28 dp top corners, drag handle, same typography. Primary action ("Sign in with Google") = accent pill; secondary actions = glass pills.
- **Menus / dialogs**: rounded 16 dp surfaces using the dock colors, `label` type. No default Material tonal surfaces.
- **Snackbar**: pill, uses the dock colors, floats 12 dp above the dock, "Undo" text action in `accent`.
- **Search field**: glass pill with a leading search icon and a trailing cancel action; 16 sp text.

## 10.8 Implementing "glass" correctly

- Default recipe: **translucent fill + 1 dp translucent border** over the gradient/grain background (`Modifier.glass(shape)`), which already gives the reference's frosted look and performs well on every API level.
- **Do not** use `Modifier.blur()` on the glass surface itself — it blurs the surface's *own content*, not what is behind it.
- True backdrop blur is optional polish. If you add it, use a maintained library (e.g. Haze), enable it only on API 31+, and keep the translucent fill as the fallback on older devices. Do not add a dependency for this unless the visual difference is clearly worth it, and tell me if you do.
- Keep glass to small surfaces (buttons, chips, search field, sheets). Do not apply blur/overdraw to the whole scrolling list.

## 10.9 Motion

- Subtle and short (100–250 ms). No looping or decorative animation.
- Chip selection: 150 ms color crossfade.
- Grid: `animateItem()` with a low-bounce spring on reorder/insert.
- Press feedback: dock items, FAB and cards scale to ≈ 0.96 for ~100 ms.
- Notes → Editor: container transform from the tapped card if it is stable, otherwise a ~220 ms fade with a slight scale.
- Header title/search collapse: ~200 ms.

## 10.10 Theme rules

- Follow the **system light/dark setting by default**, with an override (System / Light / Dark) in the Account & sync sheet, persisted in DataStore.
- `dynamicColor = false`. Never use Material You colors.
- Use `enableEdgeToEdge()`, handle insets properly, and keep status/navigation bar icon colors in sync with the theme.
- Build `NotelyTheme` around custom `CompositionLocal`s (`NotelyColors`, `NotelyTypography`, `NotelyShapes`, `NotelySpacing`). Use Material 3 components only where they help (bottom sheet, text field, snackbar host) and restyle them fully so no default Material styling is visible.
- Do not use elevation shadows anywhere except the featured card's soft shadow.

## 10.11 Visual tuning checklist

After the first working version, compare each screen against the reference image side by side (dark theme first), then tune:

- background gradient stops and grain strength
- display title size, weight, letter-spacing and spacing above/below
- chip height, padding, selected/unselected contrast
- featured card radius, padding, ghost-layer offset/rotation
- note card colors, radius, gutter, min/max height, text sizes
- dock width, height, selected-item shape, FAB size and gap
- icon stroke weight and size
- glass fill/border alpha
- contrast of text on every note color (≥ 4.5:1)
- light-theme equivalents

Do not stop once the layout works. The visual feel is part of the goal.
---

# 11. State management

Use explicit immutable UI-state classes. For example:

```kotlin
data class NotesUiState(
    val featured: NoteListItem? = null,        // most recent pinned note (filter = All)
    val pinned: List<NoteListItem> = emptyList(),
    val others: List<NoteListItem> = emptyList(),
    val filter: NotesFilter = NotesFilter.All, // All, Pinned, Synced, Local
    val sort: NotesSort = NotesSort.RecentlyEdited,
    val isSearchOpen: Boolean = false,
    val query: String = "",
    val auth: AuthState = AuthState.SignedOut,
    val syncStatus: GlobalSyncStatus = GlobalSyncStatus.Idle,
    val isLoading: Boolean = true
)

sealed interface NotesEvent {
    data class ShowUndoDelete(val noteId: String) : NotesEvent
    data class ShowMessage(val message: String) : NotesEvent
}
```

- Avoid multiple unrelated `mutableStateOf`s that can become inconsistent.
- Combine flows with `combine(...)` + `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`.
- Mark UI models `@Immutable`/`@Stable` where it helps, and use stable keys in lazy lists.
- Do not put `Context`, `Drawable`, or Firebase types in UI state.

---

# 12. Performance & reliability

- The note list must stay smooth with **thousands of notes** (use `LazyColumn` with keys; keep list-item mapping cheap).
- Never do disk/network work on the main thread.
- Truncate previews when mapping (e.g. first 200 chars) — do not pass whole bodies into list rows.
- Debounce autosave and search; avoid writing to Room on every keystroke without debounce.
- Avoid unnecessary recompositions (hoist state, use `remember`, lambdas stable, `derivedStateOf` only where it actually helps).
- Sync must never block the UI, and a sync failure must never lose or corrupt local data.
- Limit note size (e.g. 1 MB body) to respect Firestore document limits, and handle oversize gracefully with a clear message.
- Use `WorkManager` (not a foreground service) for sync.

---

# 13. Testing

Write unit tests for the pure/important logic. At minimum:

### Test 1 — Sync merge

`SyncEngine.merge`:
- local newer than remote → local wins
- remote newer than local → remote wins
- both changed, local `PENDING` → newer wins **and** a conflict copy is created for the loser
- tombstone vs edit cases

### Test 2 — Repository (with fake DAO / fake remote)

- Creating a `LOCAL_ONLY` note never calls the remote data source.
- Creating/editing a `SYNCED` note marks it `PENDING` and schedules sync.
- Delete of local-only note hard-deletes; delete of synced note tombstones.

### Test 3 — Mappers

Entity ↔ Domain ↔ Dto round-trips preserve all fields.

### Test 4 — ViewModels (with `kotlinx-coroutines-test` + fakes)

- `NotesViewModel`: featured/pinned/others split, filter chips (All/Pinned/Synced/Local), sort, search filtering, undo-delete flow.
- `EditorViewModel`: autosave debounce, empty new note is not persisted, mode toggle.

### Test 5 — Room DAO (instrumented, in-memory DB)

- Ordering (pinned first, `updatedAt` desc), soft delete exclusion, search, pending-sync query.

### Test 6 — Sync loop prevention

Merging a remote change must not mark the note `PENDING`.

Add at least one **Compose UI test** for the Notes → Editor → back flow if it is cheap. Do not write fragile screenshot tests.

---

# 14. Firebase setup deliverables

If Firebase is not yet configured, deliver these instead of guessing:

1. A short **`FIREBASE_SETUP.md`** with the exact steps:
   - Create the Firebase project and register the Android app (package name + debug/release **SHA-1 and SHA-256**).
   - Enable **Authentication → Google** provider.
   - Enable **Cloud Firestore** (production mode).
   - Download `google-services.json` into `app/`.
   - Set the **web client ID** used by Credential Manager (put it in `strings.xml` via `default_web_client_id` from the generated resources or a `local.properties`/BuildConfig field — never hard-code secrets in source).
2. A **`firestore.rules`** file:

```text
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid}/notes/{noteId} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

   Tighten it with field-type and size validation where practical.
3. Optionally a suggested `firestore.indexes.json` only if a composite index is actually needed.

Do not commit `google-services.json` to version control if the repo is public — add it to `.gitignore` and mention this.

---

# 15. Gradle / dependencies

Use a **version catalog** (`libs.versions.toml`). Add only what is needed:

- Compose BOM, Material 3, `activity-compose`, `navigation-compose`, `lifecycle-runtime-compose`, `lifecycle-viewmodel-compose`
- Hilt (`hilt-android`, compiler via KSP, `hilt-navigation-compose`, `hilt-work`, `androidx.hilt:hilt-compiler`)
- Room (`room-runtime`, `room-ktx`, compiler via KSP)
- DataStore Preferences
- WorkManager (`work-runtime-ktx`)
- Firebase BoM (`firebase-auth-ktx`/`firebase-auth`, `firebase-firestore`)
- Credential Manager (`androidx.credentials`, `credentials-play-services-auth`, `googleid`)
- Test: JUnit, `kotlinx-coroutines-test`, Turbine (optional), MockK or hand-written fakes, Room testing, Compose UI test
- Typography: **DM Sans** static TTFs bundled in `res/font/` (SIL OFL, see §10.5)
- Optional, only if clearly worth it: a backdrop-blur library such as Haze (see §10.8)

Do **not** add: Retrofit, Coil/Glide (no images), Accompanist (use built-in APIs), third-party rich-text editors, analytics or crash SDKs.

Enable R8/minification for release with the correct keep rules for Room/Firestore models if needed. Set `minSdk` sensibly (24+), `targetSdk` = latest stable.

---

# 16. Manifest & app basics

- Single `MainActivity`, `android:windowSoftInputMode="adjustResize"` (or use `imePadding()` in Compose).
- Permissions: `INTERNET` and `ACCESS_NETWORK_STATE` only. No other permissions.
- Disable default WorkManager initializer (`tools:node="remove"`) since Hilt provides the configuration.
- `android:allowBackup` — consider `false` or provide backup rules that exclude the Room DB if backup of local-only notes is not desired; decide and document.
- Adaptive launcher icon: a simple monochrome mark ("N" or a pen line). Provide a **themed (monochrome) icon** for Android 13+.
- Support predictive back (`android:enableOnBackInvokedCallback="true"`) where practical.

---

# 17. Accessibility

- Content descriptions for all icon buttons and sync glyphs ("Synced", "Waiting to sync", "Sync error").
- Minimum 48 dp touch targets, even when the visual icon is smaller.
- Sufficient contrast in both themes (WCAG AA for text). This specifically includes white text on every **note card color**, text on **glass surfaces**, and white text on the **accent**. Verify and adjust the values in §10 if they fail.
- Every action (pin, color, sync mode, delete) must be reachable from the card's ⋮ menu or the editor — no gesture-only actions.
- Works with TalkBack and large font scale without clipping.

---

# 18. Do not fake the required functionality

Do NOT:

- Store notes only in memory or in `SharedPreferences`.
- Skip Room and read directly from Firestore in the UI.
- Fake sync with `delay()`/dummy data or a hard-coded "Synced" label.
- Make Firestore the source of truth (offline must work with airplane mode on).
- Use `fallbackToDestructiveMigration()` for release.
- Use auto-increment integer ids for notes that are synced.
- Leave `TODO()` stubs in code paths that are part of the definition of done.
- Add a third page/screen to work around a layout problem.
- Hard-code colors, sizes or fonts inside composables (use the theme tokens in §10).
- Use Material default styling, dynamic color, or heavy elevation shadows.
- Fake the glass/grain look with a screenshot or static image of the reference.
- Silently swallow sync errors — surface them in the sync status.

---

# 19. Development workflow

Work in this order and keep the project compiling after each phase.

## Phase 1 — Inspect & plan
Inspect the project (§0), write the short plan.

## Phase 2 — Project foundation
Version catalog, Hilt (`@HiltAndroidApp`, modules), theme (§10), navigation with two empty routes.

## Phase 3 — Local data layer
`NoteEntity`, `NoteDao`, `NotelyDatabase`, mappers, `NoteRepository` (local only), DataStore settings.

## Phase 4 — Notes screen
Header, filter chips, featured card with ghost layers, two-column colored card grid, empty state, per-note ⋮ menu (pin / color / mode / delete) + undo, inline search, floating dock + FAB.

## Phase 5 — Editor screen
Autosave, empty-note rule, process-death safety, mode toggle, share, delete.

## Phase 6 — Auth
Credential Manager + Firebase Auth, account & sync bottom sheet (sign in/out, theme, default note type).

## Phase 7 — Sync
Remote data source, `SyncEngine`, `SyncWorker`, `SyncScheduler`, snapshot listener, conflict handling, sync-state UI.

## Phase 8 — Edge cases
Mode switching, account switching, sign out, oversize notes, network loss mid-sync, first sync of pre-existing local notes.

## Phase 9 — Tests
Unit + DAO + a basic UI test (§13).

## Phase 10 — Polish pass
Typography/spacing/color tuning against the reference image in both themes (§10.11), recomposition/perf check, accessibility check, release build with R8.

---

# 20. Claude Code working rules

You are acting as the primary engineer.

### Before coding
1. Inspect the repository and Gradle setup.
2. Make a short plan.
3. Identify anything reusable.

### While coding
- Small, coherent changes; compile after each milestone.
- Simple production-quality Kotlin; follow Kotlin and Compose idioms.
- Avoid deprecated APIs where modern replacements exist.
- No unnecessary libraries or abstractions.
- Comments only where the reasoning is non-obvious (especially in sync/merge logic).
- Keep package responsibilities clear.

### Verification after changes
Run:

```text
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

and, when a device/emulator is available, `./gradlew connectedAndroidTest`.

Manually verify (device/emulator):

- create / edit / delete / undo / pin
- search
- airplane-mode create & edit, then reconnect → notes sync
- edit the same note on two devices → conflict behavior
- local-only notes never appear in Firestore
- sign out / sign in / account switch behavior
- process death while editing (don't lose text)
- light / dark / system theme
- font scale 1.3× and TalkBack basics
- compare each screen with the reference image side by side (dark theme first, then light)

If Firebase cannot be tested (no `google-services.json`), say so clearly instead of claiming it works.

---

# 21. Definition of done

- [ ] App has **exactly two screens** (Notes, Editor); everything else is a sheet/dialog/inline.
- [ ] Notes are created, edited, pinned, deleted (with undo) and searched.
- [ ] Room is the source of truth; app works fully offline.
- [ ] Autosave works; empty new notes are not persisted; no Save button.
- [ ] Notes can be Local-only or Synced, switchable per note.
- [ ] Local-only notes never reach Firestore.
- [ ] Optional Google sign-in via Credential Manager + Firebase Auth.
- [ ] Synced notes push (WorkManager) and pull (snapshot listener + worker) correctly.
- [ ] Sync survives offline periods and retries with backoff.
- [ ] Conflicts never silently lose text.
- [ ] Deletions propagate via tombstones.
- [ ] Sync state is visible per note and globally, including errors.
- [ ] Sign-out and account switching are safe and confirmed.
- [ ] Hilt is used for all dependencies; no manual singletons.
- [ ] MVVM with immutable UI state and one-shot events.
- [ ] Theme follows §10: mid-dark taupe/graphite gradient background with grain, glass surfaces, single coral accent, no dynamic color.
- [ ] Notes screen matches the reference structure: large "Your Notes" title, filter chips, white featured card (with ghost layers), two-column colored note cards, floating dock + FAB.
- [ ] DM Sans is bundled and the type scale in §10.5 is applied (Light for large titles only).
- [ ] Per-note color (`colorId`) is stored, synced and used for the card and editor background.
- [ ] Text contrast is at least AA on every note color, on glass surfaces and on the accent.
- [ ] Light/dark follow system with an override.
- [ ] Edge-to-edge and IME insets handled correctly.
- [ ] Firestore rules and setup instructions delivered.
- [ ] Unit tests for merge logic, repository, mappers, ViewModels; DAO tests pass.
- [ ] Accessibility basics satisfied.
- [ ] `./gradlew test`, `lint`, `assembleDebug` succeed.
- [ ] Release build with R8 launches and syncs (or is clearly reported as untested).

---

# 22. Final instruction to Claude

**Do not just implement the checklist mechanically.**

First understand the product: Notely is a *calm, fast, offline-first* notebook where the cloud is a quiet convenience, not a dependency.

The highest-priority experience is:

```text
Open app
  ↓
Tap ＋
  ↓
Type immediately (keyboard up, no setup)
  ↓
Go back — note is already saved
  ↓
(If Synced) note quietly reaches the cloud when online
  ↓
Open on another device — it is there
```

Every decision should protect three things:

1. **The user's text is never lost.**
2. **The UI never waits on the network.**
3. **The screens stay soft, calm and minimal — and look like the reference.**

When you finish, provide:

1. A concise summary of what was implemented.
2. Files created/changed.
3. Important architectural decisions (especially the sync/merge design and any assumptions).
4. Tests/build commands run and their results.
5. Remaining limitations and what could not be verified (e.g. Firebase without `google-services.json`).
6. The Firebase Console steps I still need to complete.
7. A short manual QA checklist for testing Notely on a real Android device.
