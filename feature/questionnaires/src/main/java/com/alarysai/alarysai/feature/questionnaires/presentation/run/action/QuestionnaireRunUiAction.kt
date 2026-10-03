package com.alarysai.alarysai.feature.questionnaires.presentation.run.action

sealed interface QuestionnaireRunUiAction {
    /** Selects (single choice) or toggles (multiple choice) an option. */
    data class OptionToggled(val optionId: String) : QuestionnaireRunUiAction
    data class TextChanged(val text: String) : QuestionnaireRunUiAction
    data object ContinueClicked : QuestionnaireRunUiAction

    /** Optional questions only. */
    data object SkipClicked : QuestionnaireRunUiAction
    data object WatchVideoClicked : QuestionnaireRunUiAction

    /** Previous step, with its answer filled in again; on the first step it leaves the questionnaire. */
    data object BackClicked : QuestionnaireRunUiAction

    /** The X: leaves, asking first when there are answers to lose. */
    data object CloseClicked : QuestionnaireRunUiAction
    data object ExitConfirmed : QuestionnaireRunUiAction
    data object ExitDismissed : QuestionnaireRunUiAction

    /** From the review screen: reopen that step to change the answer. */
    data class EditAnswerClicked(val stepId: String) : QuestionnaireRunUiAction
    data object GenerateClicked : QuestionnaireRunUiAction
    data object Retry : QuestionnaireRunUiAction
}
