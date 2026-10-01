package com.example.notely.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.notely.data.media.AudioPlayerManager
import com.example.notely.data.media.AudioRecordingManager
import com.example.notely.data.media.RecordingState
import com.example.notely.ui.theme.NotelyTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AudioRecorderDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, file: File, durationSec: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colors = NotelyTheme.colors
    val shapes = NotelyTheme.shapes
    val spacing = NotelyTheme.spacing

    val defaultTitle = remember {
        val dateStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date())
        "Audio Note - $dateStr"
    }
    var title by remember { mutableStateOf(defaultTitle) }

    val recorder = remember { AudioRecordingManager(context) }
    val player = remember { AudioPlayerManager(context) }
    val recordInfo by recorder.recordingInfo.collectAsState()
    val playInfo by player.playbackInfo.collectAsState()

    var recordedFile by remember { mutableStateOf<File?>(null) }
    var recordedDuration by remember { mutableStateOf(0) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasPermission = granted
            if (granted) {
                recorder.startRecording()
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            recorder.cancelRecording()
            player.release()
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        shape = shapes.bottomSheet,
        color = colors.bgMid.copy(alpha = 0.98f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.screenHorizontalPadding)
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Record Audio Note",
                    style = NotelyTheme.typography.cardTitleFeatured,
                    color = colors.textPrimary,
                )
                IconButton(onClick = {
                    recorder.cancelRecording()
                    player.release()
                    onDismiss()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Note title field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title", style = NotelyTheme.typography.label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = shapes.card,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.glassBorder,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                )
            )

            Spacer(Modifier.height(28.dp))

            // Recording Status & Timer
            val displaySeconds = if (recordInfo.state != RecordingState.STOPPED) {
                recordInfo.durationSeconds
            } else {
                recordedDuration
            }
            val minutes = displaySeconds / 60
            val seconds = displaySeconds % 60
            val timerText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

            Text(
                text = timerText,
                style = NotelyTheme.typography.display,
                color = if (recordInfo.state == RecordingState.RECORDING) colors.accent else colors.textPrimary,
            )

            Spacer(Modifier.height(8.dp))

            val statusText = when (recordInfo.state) {
                RecordingState.IDLE -> if (recordedFile != null) "Recording complete" else "Tap microphone to start"
                RecordingState.RECORDING -> "Recording in progress…"
                RecordingState.PAUSED -> "Recording paused"
                RecordingState.STOPPED -> "Recording ready to save"
            }
            Text(
                text = statusText,
                style = NotelyTheme.typography.preview,
                color = colors.textSecondary,
            )

            Spacer(Modifier.height(32.dp))

            // Big record button & controls
            when (recordInfo.state) {
                RecordingState.IDLE, RecordingState.RECORDING, RecordingState.PAUSED -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Pause / Resume button if active
                        if (recordInfo.state == RecordingState.RECORDING || recordInfo.state == RecordingState.PAUSED) {
                            IconButton(
                                onClick = {
                                    if (recordInfo.state == RecordingState.RECORDING) {
                                        recorder.pauseRecording()
                                    } else {
                                        recorder.resumeRecording()
                                    }
                                },
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(colors.glassFill)
                            ) {
                                Icon(
                                    imageVector = if (recordInfo.state == RecordingState.RECORDING) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = "Pause/Resume",
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Main Record / Stop circle
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .then(
                                    if (recordInfo.state == RecordingState.RECORDING) {
                                        Modifier.scale(pulseScale)
                                    } else {
                                        Modifier
                                    }
                                )
                                .clip(CircleShape)
                                .background(colors.fabContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            IconButton(
                                onClick = {
                                    if (!hasPermission) {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    } else {
                                        when (recordInfo.state) {
                                            RecordingState.IDLE -> {
                                                recorder.startRecording()
                                            }
                                            RecordingState.RECORDING, RecordingState.PAUSED -> {
                                                val file = recorder.stopRecording()
                                                if (file != null) {
                                                    recordedFile = file
                                                    recordedDuration = recordInfo.durationSeconds
                                                }
                                            }
                                            else -> {}
                                        }
                                    }
                                },
                                modifier = Modifier.size(76.dp)
                            ) {
                                Icon(
                                    imageVector = if (recordInfo.state == RecordingState.RECORDING || recordInfo.state == RecordingState.PAUSED) {
                                        Icons.Filled.Stop
                                    } else {
                                        Icons.Filled.Mic
                                    },
                                    contentDescription = "Record",
                                    tint = colors.fabIcon,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        // Cancel button if recording
                        if (recordInfo.state == RecordingState.RECORDING || recordInfo.state == RecordingState.PAUSED) {
                            IconButton(
                                onClick = {
                                    recorder.cancelRecording()
                                },
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(colors.glassFill)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Cancel",
                                    tint = colors.danger,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }

                RecordingState.STOPPED -> {
                    // Playback preview & Save
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = {
                                recordedFile?.let { file ->
                                    if (playInfo.isPlaying) {
                                        player.pause()
                                    } else {
                                        player.play(file.absolutePath)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(colors.glassFill)
                        ) {
                            Icon(
                                imageVector = if (playInfo.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Play/Pause preview",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(Modifier.width(20.dp))

                        Button(
                            onClick = {
                                recordedFile?.let { file ->
                                    onSave(
                                        title.ifBlank { defaultTitle },
                                        file,
                                        recordedDuration
                                    )
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accent,
                                contentColor = colors.bgMid
                            ),
                            shape = shapes.pill,
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(
                                text = "Save Audio Note",
                                style = NotelyTheme.typography.cardTitle
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    TextButton(
                        onClick = {
                            recordedFile = null
                            recorder.cancelRecording()
                            player.release()
                        }
                    ) {
                        Text(
                            text = "Record Again",
                            style = NotelyTheme.typography.label,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}
