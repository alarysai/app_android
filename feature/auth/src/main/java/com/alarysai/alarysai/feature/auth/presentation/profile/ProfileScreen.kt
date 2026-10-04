package com.alarysai.alarysai.feature.auth.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.designsystem.component.AlarysBackground
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.core.designsystem.theme.Teal400
import com.alarysai.alarysai.core.designsystem.theme.Violet500
import com.alarysai.alarysai.core.ui.state.LoadingContent
import com.alarysai.alarysai.feature.auth.R
import com.alarysai.alarysai.feature.auth.presentation.components.AuthPrimaryButton
import com.alarysai.alarysai.feature.auth.presentation.components.AuthTextField

@Composable
fun ProfileScreenRoute(viewModel: ProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileUiEvent.Saved -> snackbarHostState.showSnackbar(context.getString(R.string.auth_profile_saved))
            }
        }
    }
    Box {
        ProfileScreen(uiState = uiState, onAction = viewModel::onAction)
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/**
 * SETUP: "Your profile was created" after the first sign-in. EDIT: the Profile tab, with sign-out.
 * The photo button of the mockup waits for Storage (uploads are not enabled yet).
 */
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onAction: (ProfileUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.isLoading) {
        LoadingContent(modifier.safeDrawingPadding())
        return
    }
    val isSetup = uiState.mode == ProfileMode.SETUP
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            uiState.email?.let { EmailBadge(it) }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    stringResource(if (isSetup) R.string.auth_profile_setup_title else R.string.auth_profile_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    stringResource(if (isSetup) R.string.auth_profile_setup_subtitle else R.string.auth_profile_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Avatar(uiState.initials)
            AuthTextField(
                label = stringResource(R.string.auth_name),
                value = uiState.name,
                onValueChange = { onAction(ProfileUiAction.NameChanged(it)) },
                placeholder = stringResource(R.string.auth_name_placeholder),
                errorText = if (uiState.showNameError) stringResource(R.string.auth_error_name_missing) else null,
            )
            LanguagePicker(uiState.language, onSelect = { onAction(ProfileUiAction.LanguageSelected(it)) })
            if (uiState.saveFailed) {
                Text(stringResource(R.string.auth_profile_save_failed), color = MaterialTheme.colorScheme.error)
            }
        }
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AuthPrimaryButton(
                text = stringResource(if (isSetup) R.string.auth_profile_start else R.string.auth_profile_save),
                enabled = uiState.name.isNotBlank(),
                isLoading = uiState.isSaving,
                onClick = { onAction(ProfileUiAction.SaveClicked) },
            )
            OutlinedButton(onClick = { onAction(ProfileUiAction.SignOutClicked) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(stringResource(R.string.auth_sign_out), modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun EmailBadge(email: String) {
    Row(
        modifier = Modifier
            .background(Teal400.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
            .border(1.dp, Teal400.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Teal400, modifier = Modifier.size(14.dp))
        Text(email, style = MaterialTheme.typography.labelMedium, color = Teal400)
    }
}

@Composable
private fun Avatar(initials: String) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(Violet500.copy(alpha = 0.25f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

/** Português (Brasil) / English / Español; the choice applies to the content and is saved in the profile. */
@Composable
private fun LanguagePicker(selected: Language, onSelect: (Language) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(R.string.auth_language), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Language.entries.forEach { language ->
                val isSelected = language == selected
                val shape = RoundedCornerShape(12.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.05f), shape)
                        .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.14f), shape)
                        .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(language) })
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(language.labelRes()),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

/** Each language in its own name, so it is recognizable whatever the current language is. */
private fun Language.labelRes(): Int = when (this) {
    Language.PT -> R.string.auth_language_pt
    Language.EN -> R.string.auth_language_en
    Language.ES -> R.string.auth_language_es
}

@Preview(heightDp = 760)
@Composable
private fun ProfileSetupPreview() {
    AlarysTheme {
        AlarysBackground {
            ProfileScreen(ProfileUiState(ProfileMode.SETUP, isLoading = false, email = "marina.alves@email.com", name = "Marina Alves"), onAction = {})
        }
    }
}
