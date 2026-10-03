package com.alarysai.alarysai.feature.questionnaires.presentation.run.event

sealed interface QuestionnaireRunUiEvent {
    /** Opens the external video (YouTube, Vimeo…) in its app or the browser. */
    data class OpenVideo(val url: String) : QuestionnaireRunUiEvent

    /** Back to the questionnaire list. */
    data object Exit : QuestionnaireRunUiEvent

    /** Content generation needs the server that does not exist yet. */
    data object GenerationComingSoon : QuestionnaireRunUiEvent
}
