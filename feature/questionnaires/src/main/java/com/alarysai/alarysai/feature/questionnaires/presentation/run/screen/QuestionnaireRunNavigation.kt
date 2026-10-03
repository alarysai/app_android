package com.alarysai.alarysai.feature.questionnaires.presentation.run.screen

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.alarysai.alarysai.core.navigation.QuestionnaireRunRoute

fun NavGraphBuilder.questionnaireRunDestination(onExit: () -> Unit) {
    composable(
        route = QuestionnaireRunRoute.route,
        arguments = listOf(
            navArgument(QuestionnaireRunRoute.ARG_QUESTIONNAIRE_ID) { type = NavType.StringType },
            navArgument(QuestionnaireRunRoute.ARG_TITLE) {
                type = NavType.StringType
                defaultValue = ""
            },
        ),
    ) {
        QuestionnaireRunScreenRoute(onExit = onExit)
    }
}
