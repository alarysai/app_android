package com.alarysai.alarysai.feature.questionnaires.domain.model

/** A questionnaire ready to run. [steps] are sorted by `order`, ties broken by ID (same as the panel). */
data class QuestionnaireWithSteps(
    val questionnaire: Questionnaire,
    val steps: List<Step>,
)
