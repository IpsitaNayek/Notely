# Notely — UI/UX and Functionality Update Prompt

## Context

This is an existing Android note-taking app called **Notely**. The current generated home screen contains category tabs named **Terracotta, Sage, Dusty blue**, etc.

The current design should be revised rather than rebuilding the app unnecessarily. Preserve the existing visual quality and working functionality where possible, but implement the navigation, organization, note actions, and editor features described below.

---

# 1. Remove the Meaningless Color Tabs

The current home screen has tabs/categories such as:

- Terracotta
- Sage
- Dusty blue

These names are meaningless as note categories and should be **completely removed from the user-facing navigation**.

Do not replace them with other arbitrary color names.

The category/navigation system should instead use meaningful note-management categories.

---

# 2. Home Screen Tabs

The home screen should have the following meaningful categories:

1. **All**
2. **Pinned**
3. **To-do**
4. **Notes**
5. **Audio Notes**
6. **Bin**

### All
Show every active note except permanently deleted notes.

### Pinned
Show only notes that the user has pinned.

### To-do
Show notes/tasks that are categorized as to-do items.

### Notes
Show normal text-based notes.

### Audio Notes
Show notes that contain or are primarily based around an audio recording.

### Bin
Show notes that have been deleted but are not yet permanently deleted.

The categories should be clearly understandable to a normal user.

---

# 3. Remove the Trash Icon From the Bottom Navigation Bar

The current bottom navigation contains a trash/delete icon.

Remove this trash icon from the bottom navigation.

The **Bin** should instead be accessible as a normal category/tab in the note-management interface.

Do not have two separate ways of accessing the same Bin functionality.

---

# 4. Bottom Navigation Bar

Keep the bottom navigation clean and useful.

The navigation should contain:

- **Home**
- **Search**
- **Audio Note**
- **Settings**

The existing trash icon should be removed.

The **Audio Note** option should be directly accessible from the bottom navigation.

---

# 5. Audio Note Button in Bottom Navigation

Add a dedicated **Audio Note** action in the bottom navigation.

When the user taps it:

1. Open an audio-recording interface.
2. Clearly show that recording is active.
3. Provide:
   - Start recording
   - Pause/resume recording
   - Stop recording
   - Cancel/delete recording
4. After stopping, allow the user to save the recording as an audio note.
5. The saved audio note should appear in the **Audio Notes** category.
6. The audio note should also appear in **All**.

Handle microphone permission properly using Android's runtime permission system.

The UI should make recording state obvious.

---

# 6. Long-Press Actions on Notes

In **All, Pinned, To-do, Notes, and Audio Notes**, users should be able to **long-press a note**.

Long-pressing a note should activate a contextual selection/action mode.

At minimum, provide:

- **Pin / Unpin**
- **Delete**

If a note is already pinned, the action should become **Unpin**.

If multiple-note selection is supported, allow the user to select multiple notes and perform the same actions in bulk.

Do not permanently delete a note directly from the normal note lists.

---

# 7. Delete Behavior

When the user chooses **Delete**:

- Move the note to the **Bin**.
- Do not permanently delete it immediately.
- Remove it from normal active categories.
- Keep its content available inside Bin.

The deleted note should no longer appear in:

- All
- Pinned
- To-do
- Notes
- Audio Notes

unless the design intentionally indicates its deleted state.

---

# 8. Bin Screen

The Bin should display all deleted notes.

Each deleted note should retain enough information to identify it, such as:

- Title
- Short preview
- Note type
- Deletion status/date where appropriate

Provide a prominent but controlled action:

### Empty Bin

When the user chooses **Empty Bin**:

1. Show a confirmation dialog.
2. Clearly warn that this action permanently deletes the notes.
3. Provide:
   - Cancel
   - Permanently Delete / Empty Bin
4. Only after confirmation should the notes be permanently deleted.

The permanent deletion operation must be irreversible.

Do not accidentally delete active notes when emptying the Bin.

---

# 9. Note Editor

Every normal note should have a proper rich note-editing experience.

Inside the note editor, provide formatting/content options including:

### Bullet List

Allow users to create bullet points.

Example:

- Item one
- Item two
- Item three

### Numbered List

Allow numbered lists.

Example:

1. First item
2. Second item
3. Third item

### Checkboxes / To-do Items

Allow users to insert interactive checkboxes.

Example:

☐ Buy groceries  
☐ Complete assignment  
☑ Submit project

Checking/unchecking an item should update its state.

### Photo/Image

Allow users to add photos/images to a note.

Provide an option to:

- Select an image from the device
- Add it to the note

If appropriate, also support taking a photo using the camera.

Handle storage permissions and camera permissions according to the Android version being targeted.

### Audio

Allow users to attach an audio recording to a note.

The note should display an audio attachment/player with controls such as:

- Play
- Pause
- Progress
- Duration

The user should be able to listen to the attached recording later.

---

# 10. Note Types

The application should distinguish between:

### Normal Note
A standard text note.

### To-do Note
A note containing task/checklist content.

### Audio Note
A note centered around an audio recording.

A note can still contain mixed content where appropriate, for example:

- Text
- Bullet lists
- Numbered lists
- Checkboxes
- Images
- Audio

Do not unnecessarily restrict users to only one content type.

---

# 11. Creating Notes

The existing floating **+** button can remain as the primary note creation button.

When the user taps **+**, provide a clear choice such as:

- New Note
- New To-do
- Audio Note

However, the dedicated Audio Note bottom-navigation action should remain available as a quick shortcut.

Avoid creating duplicate or confusing entry points.

---

# 12. Search

Keep the Search section in the bottom navigation.

The search functionality should search through active notes and provide useful results based on:

- Note title
- Note text/content
- To-do content

Optionally include audio-note titles/metadata.

Deleted notes should either be excluded from normal search or searched separately inside Bin.

---

# 13. Pinning

Pinned notes should have a clear visual indication, such as a small pin icon.

Pinning should not create a duplicate note.

A pinned note should remain in its original category while also appearing under **Pinned**.

For example:

A pinned normal note should appear in:

- All
- Notes
- Pinned

A pinned to-do note should appear in:

- All
- To-do
- Pinned

---

# 14. Home Screen Layout

Keep the existing clean, minimal aesthetic from the current design.

The current large heading:

> Your Notes

can remain.

Below it, show the note count dynamically, for example:

> 1 note

or

> 5 notes

The category tabs should now be:

**All | Pinned | To-do | Notes | Audio Notes | Bin**

If all tabs do not fit comfortably on one line, use a horizontally scrollable tab row.

Do not shrink the text excessively just to fit every category.

---

# 15. Note Cards

Improve the note cards so that they clearly communicate the note's content and state.

A note card can contain:

- Title
- Short content preview
- Last edited time
- Pin indicator when pinned
- Note type indicator when useful
- Audio indicator for audio notes
- To-do/checklist indicator for to-do notes

Keep cards visually simple and consistent.

Do not overload cards with too many icons.

---

# 16. Swipe vs Long Press

Do not make destructive actions too easy to trigger accidentally.

The primary delete action should be available through:

**Long press → Delete**

You may optionally support swipe-to-delete, but if implemented it must have an undo mechanism.

Long press must remain available.

---

# 17. Undo After Delete

After moving a note to Bin, show a temporary snackbar/toast-style confirmation such as:

> Note moved to Bin

Provide an **Undo** action where practical.

Undo should restore the note to its previous active state/category.

---

# 18. Data Architecture

Implement the changes using a clean local data model.

If the project is already using **Room Database**, continue using Room rather than introducing another local database.

A note should contain enough information to support:

- ID
- Title
- Content
- Note type
- Created timestamp
- Updated timestamp
- Pinned state
- Deleted/bin state
- Optional deletion timestamp

For richer content, design the data model appropriately rather than storing everything in an unnecessarily complicated single field.

For example, consider separate entities/models for:

- Notes
- Checklist items
- Image attachments
- Audio attachments

Use relationships where appropriate.

---

# 19. Firebase / Cloud Sync

If Firebase is already implemented in the project, preserve the existing Firebase architecture.

Do not replace working Firebase functionality unnecessarily.

If synchronization is implemented, make sure:

- Pinned state syncs
- Deleted/bin state syncs
- Note content syncs
- Attachments are handled appropriately

Permanent deletion from Bin should also be reflected in the cloud database/storage if cloud sync is enabled.

Do not upload large audio/image files directly into a database field. Use appropriate cloud storage for binary attachments.

---

# 20. Android Technology

Use the existing Android architecture of the project where possible.

Prefer:

- **Kotlin**
- **Jetpack Compose**
- **Material 3**
- **Room**
- **ViewModel**
- **StateFlow / Flow**
- **Navigation Compose**
- **Hilt** for dependency injection if already being used
- **Firebase Authentication / Firestore / Storage** where cloud functionality is already part of the project

For media:

- Android audio recording APIs / MediaRecorder or an appropriate modern Android-compatible solution
- ExoPlayer/Media3 for reliable audio playback
- Android Photo Picker for selecting images where appropriate

Use lifecycle-aware state collection.

Do not introduce unnecessary libraries.

---

# 21. UI/UX Requirements

Maintain the current soft, modern, minimal aesthetic.

The interface should feel like a real production note-taking application rather than a demo.

Important UX principles:

- Clear labels
- Meaningful category names
- Consistent spacing
- Large enough touch targets
- Clear selected/unselected tab states
- Proper empty states
- Confirmation dialogs for destructive actions
- Snackbar feedback for actions
- Accessible contrast
- Smooth scrolling
- No unnecessary decorative elements

---

# 22. Empty States

Every category should have an appropriate empty state.

Examples:

### Pinned
> No pinned notes yet

### To-do
> No to-do notes yet

### Notes
> No notes yet

### Audio Notes
> No audio notes yet

### Bin
> Bin is empty

For Bin, also make the **Empty Bin** action unavailable or hidden when there are no deleted notes.

---

# 23. Settings

Keep Settings in the bottom navigation.

Do not put note-management functionality such as Bin inside Settings.

Settings can contain application preferences such as:

- Theme
- Notifications
- Storage/sync preferences
- Account
- About

Only implement settings that are actually supported by the application.

---

# 24. Important Behavioral Rules

Implement these rules consistently:

1. **Delete = move to Bin**
2. **Empty Bin = permanent deletion**
3. **Pin = note appears in Pinned but remains in its original category**
4. **Unpin = remove from Pinned but keep the note**
5. **Long press = show note actions**
6. **Audio Note = accessible directly from bottom navigation**
7. **To-do = checklist/task-oriented notes**
8. **Notes = normal text notes**
9. **Audio Notes = notes containing/centered around recordings**
10. **All = all active notes**
11. **Bin = deleted notes only**
12. **Do not use arbitrary color names as categories**

---

# 25. Navigation Structure

Use a clear navigation structure similar to:

```text
Bottom Navigation
│
├── Home
│   ├── All
│   ├── Pinned
│   ├── To-do
│   ├── Notes
│   ├── Audio Notes
│   └── Bin
│
├── Search
│
├── Audio Note
│   └── Recording Screen
│
└── Settings
```

The **+** button can open:

```text
New Note
New To-do
Audio Note
```

---

# 26. Implementation Instructions for the Existing Project

Do not generate a completely separate application.

Modify the existing Notely project.

Before changing code:

1. Inspect the existing project structure.
2. Identify the current navigation implementation.
3. Identify the current note model/entity.
4. Identify the Room database and DAOs.
5. Identify existing ViewModels and repositories.
6. Identify the current Home screen and note card components.
7. Identify existing Firebase integration if present.
8. Reuse existing components where practical.
9. Refactor only where necessary.

Avoid duplicate screens, duplicate models, or duplicate database implementations.

---

# 27. Final Expected User Flow

### Creating a normal note

```text
Home
→ +
→ New Note
→ Write content
→ Add bullets / numbering / checkbox / image / audio
→ Save
→ Appears in All + Notes
```

### Creating a to-do

```text
Home
→ +
→ New To-do
→ Add checklist items
→ Save
→ Appears in All + To-do
```

### Creating an audio note

```text
Bottom Navigation
→ Audio Note
→ Record
→ Stop
→ Save
→ Appears in All + Audio Notes
```

### Pinning

```text
Long press note
→ Pin
→ Note appears in Pinned
```

### Deleting

```text
Long press note
→ Delete
→ Note moves to Bin
```

### Restoring

```text
Bin
→ Select/open deleted note
→ Restore
→ Note returns to its previous active category
```

### Permanently deleting

```text
Bin
→ Empty Bin
→ Confirmation dialog
→ Permanently Delete
→ Notes are permanently removed
```

---

# 28. Critical Requirement

The goal is to make the application **meaningful and intuitive as a note-taking app**.

Do not use categories merely for visual decoration.

Remove:

- Terracotta
- Sage
- Dusty blue
- Any other arbitrary color-name categories
- Trash icon from bottom navigation

Add:

- All
- Pinned
- To-do
- Notes
- Audio Notes
- Bin
- Audio Note shortcut in bottom navigation
- Long-press note actions
- Pin/unpin
- Delete-to-Bin behavior
- Empty Bin / permanent deletion
- Rich note content: bullets, numbering, checkboxes, images, and audio

Keep the existing visual design polished, but prioritize **clear information architecture, usability, and correct functionality** over decorative UI.
