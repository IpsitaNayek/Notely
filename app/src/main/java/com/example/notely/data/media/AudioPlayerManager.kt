package com.example.notely.data.media

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

data class PlaybackInfo(
    val isPlaying: Boolean = false,
    val currentPositionMs: Int = 0,
    val totalDurationMs: Int = 0,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * Robust audio player supporting file paths and content URIs, with AudioAttributes and progress tracking.
 */
class AudioPlayerManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private var currentSource: String? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private val _playbackInfo = MutableStateFlow(PlaybackInfo())
    val playbackInfo: StateFlow<PlaybackInfo> = _playbackInfo.asStateFlow()

    companion object {
        // Global reference to ensure only one audio plays at any time
        private var activePlayer: AudioPlayerManager? = null

        fun stopActivePlayer() {
            activePlayer?.stop()
            activePlayer = null
        }
    }

    fun play(pathOrUri: String) {
        if (pathOrUri.isBlank()) {
            _playbackInfo.update { it.copy(errorMessage = "Audio file path is empty") }
            return
        }

        // If another player is active, stop it
        if (activePlayer != null && activePlayer != this) {
            activePlayer?.stop()
        }
        activePlayer = this

        try {
            // If already prepared for this source and paused/completed, just resume
            if (mediaPlayer != null && currentSource == pathOrUri) {
                requestAudioFocus()
                if (_playbackInfo.value.isCompleted) {
                    mediaPlayer?.seekTo(0)
                }
                mediaPlayer?.start()
                _playbackInfo.update {
                    it.copy(
                        isPlaying = true,
                        isCompleted = false,
                        errorMessage = null,
                    )
                }
                startProgressTracker()
                return
            }

            // Otherwise initialize fresh
            releasePlayerOnly()
            currentSource = pathOrUri

            val player = MediaPlayer()
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
            player.setAudioAttributes(audioAttributes)

            if (pathOrUri.startsWith("content://")) {
                val uri = Uri.parse(pathOrUri)
                player.setDataSource(context, uri)
            } else {
                val file = File(pathOrUri)
                if (!file.exists()) {
                    _playbackInfo.update {
                        it.copy(
                            isPlaying = false,
                            errorMessage = "Audio file not found: ${file.name}",
                        )
                    }
                    return
                }
                player.setDataSource(file.absolutePath)
            }

            player.setOnPreparedListener { mp ->
                requestAudioFocus()
                mp.start()
                val total = mp.duration
                _playbackInfo.update {
                    it.copy(
                        isPlaying = true,
                        totalDurationMs = total,
                        isCompleted = false,
                        errorMessage = null,
                    )
                }
                startProgressTracker()
            }

            player.setOnCompletionListener {
                progressJob?.cancel()
                abandonAudioFocus()
                _playbackInfo.update {
                    it.copy(
                        isPlaying = false,
                        currentPositionMs = 0,
                        isCompleted = true,
                    )
                }
            }

            player.setOnErrorListener { _, what, extra ->
                progressJob?.cancel()
                abandonAudioFocus()
                _playbackInfo.update {
                    it.copy(
                        isPlaying = false,
                        errorMessage = "Cannot play audio (code $what, $extra)",
                    )
                }
                true
            }

            mediaPlayer = player
            player.prepareAsync()

        } catch (e: Exception) {
            e.printStackTrace()
            abandonAudioFocus()
            releasePlayerOnly()
            _playbackInfo.update {
                it.copy(
                    isPlaying = false,
                    errorMessage = e.localizedMessage ?: "Failed to play audio",
                )
            }
        }
    }

    fun pause() {
        try {
            mediaPlayer?.pause()
            progressJob?.cancel()
            abandonAudioFocus()
            _playbackInfo.update { it.copy(isPlaying = false) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun seekTo(positionMs: Int) {
        try {
            mediaPlayer?.seekTo(positionMs)
            _playbackInfo.update { it.copy(currentPositionMs = positionMs) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        progressJob?.cancel()
        abandonAudioFocus()
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }
        _playbackInfo.update {
            it.copy(
                isPlaying = false,
                currentPositionMs = 0,
            )
        }
    }

    fun release() {
        if (activePlayer == this) {
            activePlayer = null
        }
        progressJob?.cancel()
        abandonAudioFocus()
        releasePlayerOnly()
        currentSource = null
        _playbackInfo.value = PlaybackInfo()
    }

    private fun releasePlayerOnly() {
        try {
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && mediaPlayer?.isPlaying == true) {
                val current = mediaPlayer?.currentPosition ?: 0
                val total = mediaPlayer?.duration ?: _playbackInfo.value.totalDurationMs
                _playbackInfo.update {
                    it.copy(
                        currentPositionMs = current,
                        totalDurationMs = total,
                    )
                }
                delay(200)
            }
        }
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                .setOnAudioFocusChangeListener { focusChange ->
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS ||
                        focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                    ) {
                        pause()
                    }
                }
                .build()
            audioFocusRequest = req
            audioManager?.requestAudioFocus(req)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                { focusChange ->
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS ||
                        focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                    ) {
                        pause()
                    }
                },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(null)
        }
    }
}
