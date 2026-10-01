package com.example.notely.data.model

import java.util.UUID

sealed class NoteContentBlock {
    abstract val id: String

    data class Text(
        override val id: String = UUID.randomUUID().toString(),
        val content: String = "",
    ) : NoteContentBlock()

    data class Image(
        override val id: String = UUID.randomUUID().toString(),
        val uri: String,
    ) : NoteContentBlock()

    data class Audio(
        override val id: String = UUID.randomUUID().toString(),
        val path: String,
        val durationSec: Int = 0,
    ) : NoteContentBlock()

    companion object {
        val IMAGE_TAG_REGEX = Regex("\\[photo:([^\\]]+)\\]")
        val AUDIO_TAG_REGEX = Regex("\\[audio:([^\\]:]+):?(\\d+)?\\]")
        val BLOCK_TAG_REGEX = Regex("\\[(photo|audio):([^\\]]+)\\]")

        /**
         * Parses a note body string into sequential content blocks.
         */
        fun parseBody(rawBody: String): List<NoteContentBlock> {
            if (rawBody.isBlank()) {
                return listOf(Text(content = ""))
            }

            val blocks = mutableListOf<NoteContentBlock>()
            var lastIndex = 0

            for (match in BLOCK_TAG_REGEX.findAll(rawBody)) {
                val start = match.range.first
                val end = match.range.last + 1

                if (start > lastIndex) {
                    val text = rawBody.substring(lastIndex, start)
                    blocks.add(Text(content = text))
                }

                val type = match.groupValues[1]
                val content = match.groupValues[2]

                if (type == "photo") {
                    blocks.add(Image(uri = content))
                } else if (type == "audio") {
                    val parts = content.split(":")
                    val path = parts.getOrNull(0) ?: ""
                    val duration = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    blocks.add(Audio(path = path, durationSec = duration))
                }

                lastIndex = end
            }

            if (lastIndex < rawBody.length) {
                val remainingText = rawBody.substring(lastIndex)
                blocks.add(Text(content = remainingText))
            }

            if (blocks.isEmpty()) {
                blocks.add(Text(content = ""))
            }

            return blocks
        }

        /**
         * Serializes blocks back into note body string preserving relative positions.
         */
        fun serializeBody(blocks: List<NoteContentBlock>): String {
            return buildString {
                for (block in blocks) {
                    when (block) {
                        is Text -> {
                            append(block.content)
                        }
                        is Image -> {
                            append("\n\n[photo:${block.uri}]\n\n")
                        }
                        is Audio -> {
                            append("\n\n[audio:${block.path}:${block.durationSec}]\n\n")
                        }
                    }
                }
            }.trim()
        }

        fun stripMediaTags(body: String): String {
            return body
                .replace(IMAGE_TAG_REGEX, "")
                .replace(AUDIO_TAG_REGEX, "")
                .replace(Regex("\\n{3,}"), "\n\n")
                .trim()
        }

        fun extractAllImages(body: String): List<String> {
            return IMAGE_TAG_REGEX.findAll(body).map { it.groupValues[1] }.toList()
        }

        fun extractAllAudio(body: String): List<Pair<String, Int>> {
            return AUDIO_TAG_REGEX.findAll(body).map {
                val path = it.groupValues[1]
                val duration = it.groupValues[2].toIntOrNull() ?: 0
                Pair(path, duration)
            }.toList()
        }
    }
}
