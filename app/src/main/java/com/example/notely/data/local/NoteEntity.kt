package com.example.notely.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Note types supported by Notely:
 * - [NORMAL]: Standard text note with optional rich elements
 * - [TODO]: Checklist / task-oriented note
 * - [AUDIO]: Audio recording centered note
 */
enum class NoteType {
    NORMAL,
    TODO,
    AUDIO,
}

data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val isChecked: Boolean = false,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("text", text)
        put("isChecked", isChecked)
    }

    companion object {
        fun fromJson(json: JSONObject): ChecklistItem = ChecklistItem(
            id = json.optString("id", UUID.randomUUID().toString()),
            text = json.optString("text", ""),
            isChecked = json.optBoolean("isChecked", false),
        )

        fun listToJson(items: List<ChecklistItem>): String {
            val array = JSONArray()
            items.forEach { array.put(it.toJson()) }
            return array.toString()
        }

        fun listFromJson(jsonStr: String): List<ChecklistItem> {
            if (jsonStr.isBlank()) return emptyList()
            return try {
                val array = JSONArray(jsonStr)
                List(array.length()) { i -> fromJson(array.getJSONObject(i)) }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}

/**
 * Room entity representing a single note.
 *
 * - [id]: UUID string, generated at creation time.
 * - [colorId]: Index into [NotelyColors.noteColors] (0..5).
 * - [isPinned]: Pinned notes float to the top of the grid and appear in Pinned tab.
 * - [isTrashed]: Soft-deleted notes are hidden from the main view and appear in Bin.
 * - [createdAt]: Epoch millis when the note was first created.
 * - [updatedAt]: Epoch millis of the last content edit.
 * - [syncStatus]: 0 = synced, 1 = pending upload, 2 = pending delete.
 * - [noteType]: [NoteType.NORMAL], [NoteType.TODO], or [NoteType.AUDIO].
 * - [checklistJson]: Serialized JSON of [ChecklistItem]s for checklists/to-dos.
 * - [imageUri]: Attached image URI/path.
 * - [audioPath]: Attached audio file path.
 * - [audioDurationSec]: Duration of the audio recording in seconds.
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val body: String = "",
    val colorId: Int = 0,
    val isPinned: Boolean = false,
    val isTrashed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: Int = SYNC_PENDING_UPLOAD,
    val noteType: String = NoteType.NORMAL.name,
    val checklistJson: String = "",
    val imageUri: String = "",
    val audioPath: String = "",
    val audioDurationSec: Int = 0,
) {
    fun getChecklist(): List<ChecklistItem> = ChecklistItem.listFromJson(checklistJson)

    fun getFirstImageUri(): String {
        val inBody = com.example.notely.data.model.NoteContentBlock.extractAllImages(body).firstOrNull()
        return if (!inBody.isNullOrBlank()) inBody else imageUri
    }

    fun getFirstAudio(): Pair<String, Int>? {
        val inBody = com.example.notely.data.model.NoteContentBlock.extractAllAudio(body).firstOrNull()
        if (inBody != null && inBody.first.isNotBlank()) return inBody
        if (audioPath.isNotBlank()) return Pair(audioPath, audioDurationSec)
        return null
    }

    fun getCleanBodyPreview(): String {
        return com.example.notely.data.model.NoteContentBlock.stripMediaTags(body)
    }

    fun isAudioNote(): Boolean = noteType == NoteType.AUDIO.name || getFirstAudio() != null
    fun isTodoNote(): Boolean = noteType == NoteType.TODO.name || checklistJson.isNotBlank()

    companion object {
        const val SYNC_SYNCED = 0
        const val SYNC_PENDING_UPLOAD = 1
        const val SYNC_PENDING_DELETE = 2
    }
}
