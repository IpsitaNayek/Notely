package com.example.notely.data.media

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.SystemClock
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

enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED,
    STOPPED,
}

data class RecordingInfo(
    val state: RecordingState = RecordingState.IDLE,
    val durationSeconds: Int = 0,
    val amplitude: Int = 0,
    val outputFile: File? = null,
)

class AudioRecordingManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private var accumulatedDurationMs = 0L
    private var segmentStartTimeMs = 0L

    private val _recordingInfo = MutableStateFlow(RecordingInfo())
    val recordingInfo: StateFlow<RecordingInfo> = _recordingInfo.asStateFlow()

    fun startRecording(): Boolean {
        return try {
            val audioDir = File(context.filesDir, "audio").apply { if (!exists()) mkdirs() }
            val file = File(audioDir, "audio_note_${System.currentTimeMillis()}.m4a")
            currentOutputFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            accumulatedDurationMs = 0L
            segmentStartTimeMs = SystemClock.elapsedRealtime()

            _recordingInfo.value = RecordingInfo(
                state = RecordingState.RECORDING,
                durationSeconds = 0,
                amplitude = 0,
                outputFile = file,
            )

            startTimer()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            cleanup()
            false
        }
    }

    fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.pause()
                timerJob?.cancel()
                accumulatedDurationMs += SystemClock.elapsedRealtime() - segmentStartTimeMs
                _recordingInfo.update { it.copy(state = RecordingState.PAUSED) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.resume()
                segmentStartTimeMs = SystemClock.elapsedRealtime()
                _recordingInfo.update { it.copy(state = RecordingState.RECORDING) }
                startTimer()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopRecording(): File? {
        timerJob?.cancel()
        if (_recordingInfo.value.state == RecordingState.RECORDING) {
            accumulatedDurationMs += SystemClock.elapsedRealtime() - segmentStartTimeMs
        }
        val finalSec = (accumulatedDurationMs / 1000).toInt()

        return try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: RuntimeException) {
                    e.printStackTrace()
                }
                reset()
                release()
            }
            mediaRecorder = null
            val file = currentOutputFile
            _recordingInfo.update {
                it.copy(
                    state = RecordingState.STOPPED,
                    durationSeconds = if (finalSec > 0) finalSec else 1,
                )
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            cleanup()
            null
        }
    }

    fun cancelRecording() {
        timerJob?.cancel()
        cleanup()
        currentOutputFile?.let {
            if (it.exists()) it.delete()
        }
        currentOutputFile = null
        accumulatedDurationMs = 0L
        _recordingInfo.value = RecordingInfo(state = RecordingState.IDLE)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && _recordingInfo.value.state == RecordingState.RECORDING) {
                val currentSegmentElapsed = SystemClock.elapsedRealtime() - segmentStartTimeMs
                val totalElapsedSec = ((accumulatedDurationMs + currentSegmentElapsed) / 1000).toInt()
                val amp = try {
                    mediaRecorder?.maxAmplitude ?: 0
                } catch (_: Exception) {
                    0
                }
                _recordingInfo.update {
                    it.copy(
                        durationSeconds = totalElapsedSec,
                        amplitude = amp,
                    )
                }
                delay(200)
            }
        }
    }

    private fun cleanup() {
        try {
            mediaRecorder?.reset()
            mediaRecorder?.release()
        } catch (_: Exception) {
        }
        mediaRecorder = null
    }
}
