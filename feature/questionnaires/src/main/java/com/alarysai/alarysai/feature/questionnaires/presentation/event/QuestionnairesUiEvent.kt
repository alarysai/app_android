package com.alarysai.alarysai.feature.questionnaires.presentation.event

sealed interface QuestionnairesUiEvent {
    /** Opens a questionnaire; the title shows while its steps load. */
    data class OpenQuestionnaire(val questionnaireId: String, val title: String) : QuestionnairesUiEvent
}
