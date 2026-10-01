package com.example.notely.ui.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notely.BuildConfig
import com.example.notely.ui.theme.NotelyBackground
import com.example.notely.ui.theme.NotelyTheme
import kotlinx.coroutines.delay

/**
 * Settings screen — navigated to from the dock Settings icon or route.
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    NotelyBackground {
        SettingsContent(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            viewModel = viewModel,
        )
    }
}

/**
 * Reusable settings content for embed in NotesScreen or standalone.
 */
@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val colors = NotelyTheme.colors
    val spacing = NotelyTheme.spacing

    // Auto-clear messages after 3s
    LaunchedEffect(authState.successMessage) {
        if (authState.successMessage != null) {
            delay(3000)
            authViewModel.clearMessages()
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.screenHorizontalPadding),
    ) {
        Spacer(Modifier.height(spacing.headerSpacing))

        Text(
            text = "Settings",
            style = NotelyTheme.typography.display,
            color = colors.textPrimary,
        )

        Spacer(Modifier.height(32.dp))

        // ── Account / Auth ──
        SectionHeader(title = "Account")
        Spacer(Modifier.height(12.dp))

        AuthSection(authState = authState, authViewModel = authViewModel)

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = colors.glassBorder)
        Spacer(Modifier.height(24.dp))

        // ── Appearance ──
        SectionHeader(title = "Appearance")
        Spacer(Modifier.height(12.dp))

        val options = listOf(
            null to "Device mode",
            false to "Light mode",
            true to "Dark mode",
        )
        Column(modifier = Modifier.selectableGroup()) {
            options.forEach { (value, label) ->
                ThemeOption(
                    label = label,
                    selected = uiState.darkThemeOverride == value,
                    onClick = { viewModel.onThemeModeChanged(value) },
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = colors.glassBorder)
        Spacer(Modifier.height(24.dp))

        // ── About ──
        SectionHeader(title = "About")
        Spacer(Modifier.height(12.dp))

        LabelValueRow(label = "Version", value = BuildConfig.VERSION_NAME)
        LabelValueRow(label = "Build", value = BuildConfig.VERSION_CODE.toString())

        Spacer(Modifier.height(32.dp))
    }

    // Password reset dialog
    if (authState.showResetDialog) {
        var resetEmail by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { authViewModel.showResetDialog(false) },
            title = { Text("Reset Password", color = NotelyTheme.colors.textPrimary) },
            text = {
                Column {
                    Text(
                        "Enter your email to receive a password reset link.",
                        style = NotelyTheme.typography.body,
                        color = NotelyTheme.colors.textSecondary,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done,
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NotelyTheme.colors.accent,
                            unfocusedBorderColor = NotelyTheme.colors.glassBorder,
                            focusedTextColor = NotelyTheme.colors.textPrimary,
                            unfocusedTextColor = NotelyTheme.colors.textPrimary,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { authViewModel.sendPasswordReset(resetEmail) }) {
                    Text("Send Link", color = NotelyTheme.colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { authViewModel.showResetDialog(false) }) {
                    Text("Cancel", color = NotelyTheme.colors.textSecondary)
                }
            },
            containerColor = NotelyTheme.colors.bgMid,
            shape = NotelyTheme.shapes.menu,
        )
    }
}

// ── Auth Section ─────────────────────────────────────────────────────────

@Composable
private fun AuthSection(
    authState: AuthUiState,
    authViewModel: AuthViewModel,
) {
    val colors = NotelyTheme.colors
    val shapes = NotelyTheme.shapes

    AnimatedContent(
        targetState = authState.currentUser != null,
        label = "auth_section",
    ) { isSignedIn ->
        if (isSignedIn) {
            // ── Signed-in card ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = shapes.card,
                color = colors.glassFill,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Avatar circle
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(colors.accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Account",
                            tint = colors.accent,
                            modifier = Modifier.size(36.dp),
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = authState.currentUser?.email ?: "Unknown",
                        style = NotelyTheme.typography.cardTitle,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Signed in",
                            style = NotelyTheme.typography.meta,
                            color = colors.textSecondary,
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Success / error feedback
                    if (authState.successMessage != null) {
                        FeedbackText(message = authState.successMessage, isError = false)
                        Spacer(Modifier.height(8.dp))
                    }
                    if (authState.errorMessage != null) {
                        FeedbackText(message = authState.errorMessage, isError = true)
                        Spacer(Modifier.height(8.dp))
                    }

                    Button(
                        onClick = { authViewModel.signOut() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.danger.copy(alpha = 0.15f),
                            contentColor = colors.danger,
                        ),
                        shape = shapes.pill,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Sign Out",
                            style = NotelyTheme.typography.cardTitle,
                        )
                    }
                }
            }
        } else {
            // ── Auth form ──
            AuthForm(authState = authState, authViewModel = authViewModel)
        }
    }
}

@Composable
private fun AuthForm(
    authState: AuthUiState,
    authViewModel: AuthViewModel,
) {
    val colors = NotelyTheme.colors
    val shapes = NotelyTheme.shapes

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = shapes.card,
        color = colors.glassFill,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Icon header
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colors.accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(32.dp),
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = if (authState.isSignUpMode) "Create Account" else "Sign In",
                style = NotelyTheme.typography.cardTitleFeatured,
                color = colors.textPrimary,
            )

            Spacer(Modifier.height(16.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                colors = authTextFieldColors(colors),
                modifier = Modifier.fillMaxWidth(),
                shape = shapes.card,
            )

            Spacer(Modifier.height(10.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (authState.isSignUpMode) ImeAction.Next else ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (!authState.isSignUpMode) {
                            authViewModel.signIn(email, password)
                        }
                    }
                ),
                colors = authTextFieldColors(colors),
                modifier = Modifier.fillMaxWidth(),
                shape = shapes.card,
            )

            // Confirm password (sign-up only)
            if (authState.isSignUpMode) {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            authViewModel.signUp(email, password, confirmPassword)
                        }
                    ),
                    colors = authTextFieldColors(colors),
                    modifier = Modifier.fillMaxWidth(),
                    shape = shapes.card,
                )
            }

            Spacer(Modifier.height(16.dp))

            // Error / success messages
            if (authState.errorMessage != null) {
                FeedbackText(message = authState.errorMessage, isError = true)
                Spacer(Modifier.height(8.dp))
            }
            if (authState.successMessage != null) {
                FeedbackText(message = authState.successMessage, isError = false)
                Spacer(Modifier.height(8.dp))
            }

            // Primary action button
            Button(
                onClick = {
                    if (authState.isSignUpMode) {
                        authViewModel.signUp(email, password, confirmPassword)
                    } else {
                        authViewModel.signIn(email, password)
                    }
                },
                enabled = !authState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = colors.bgMid,
                ),
                shape = shapes.pill,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = colors.bgMid,
                    )
                } else {
                    Text(
                        text = if (authState.isSignUpMode) "Create Account" else "Sign In",
                        style = NotelyTheme.typography.cardTitle,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Toggle sign-in / sign-up
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (authState.isSignUpMode) "Already have an account?" else "Don't have an account?",
                    style = NotelyTheme.typography.meta,
                    color = colors.textSecondary,
                )
                TextButton(onClick = { authViewModel.toggleMode() }) {
                    Text(
                        text = if (authState.isSignUpMode) "Sign In" else "Sign Up",
                        style = NotelyTheme.typography.meta,
                        color = colors.accent,
                    )
                }
            }

            // Forgot password (sign-in mode only)
            if (!authState.isSignUpMode) {
                TextButton(
                    onClick = { authViewModel.showResetDialog(true) },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text(
                        text = "Forgot password?",
                        style = NotelyTheme.typography.meta,
                        color = colors.textTertiary,
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedbackText(message: String, isError: Boolean) {
    val colors = NotelyTheme.colors
    Text(
        text = message,
        style = NotelyTheme.typography.meta,
        color = if (isError) colors.danger else colors.accent,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun authTextFieldColors(colors: com.example.notely.ui.theme.NotelyColors) =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.accent,
        unfocusedBorderColor = colors.glassBorder,
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary,
        focusedLabelColor = colors.accent,
        unfocusedLabelColor = colors.textTertiary,
        cursorColor = colors.accent,
    )

// ── Sub-composables ──────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = NotelyTheme.typography.label,
        color = NotelyTheme.colors.textTertiary,
    )
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = NotelyTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = colors.accent,
                unselectedColor = colors.textTertiary,
            ),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = NotelyTheme.typography.body,
            color = colors.textPrimary,
        )
    }
}

@Composable
private fun LabelValueRow(label: String, value: String) {
    val colors = NotelyTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = NotelyTheme.typography.body,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = NotelyTheme.typography.body,
            color = colors.textSecondary,
        )
    }
}
