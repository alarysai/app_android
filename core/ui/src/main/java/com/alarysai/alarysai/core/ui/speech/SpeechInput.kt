package com.alarysai.alarysai.core.ui.speech

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Starts the system speech recognizer and returns the best transcription.
 * [launch] is null when the device has no recognizer, so callers can hide the mic button.
 * The `<queries>` entry in this module's manifest makes the recognizer visible on Android 11+.
 */
class SpeechInput(val launch: (() -> Unit)?)

@Composable
fun rememberSpeechInput(onResult: (String) -> Unit): SpeechInput {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            ?.takeIf { it.isNotBlank() }
            ?.let(onResult)
    }
    val intent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    }
    val isAvailable = remember { intent.resolveActivity(context.packageManager) != null }
    return remember(isAvailable, launcher) {
        SpeechInput(launch = if (isAvailable) ({ launcher.launch(intent) }) else null)
    }
}
