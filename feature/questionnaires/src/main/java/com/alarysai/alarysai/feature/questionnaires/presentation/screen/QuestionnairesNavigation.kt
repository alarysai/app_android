package com.alarysai.alarysai.feature.questionnaires.presentation.screen

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.alarysai.alarysai.core.navigation.QuestionnairesRoute

fun NavGraphBuilder.questionnairesDestination(
    onBack: () -> Unit,
    onOpenQuestionnaire: (questionnaireId: String, title: String) -> Unit,
) {
    composable(
        route = QuestionnairesRoute.route,
        arguments = listOf(
            navArgument(QuestionnairesRoute.ARG_CATEGORY_ID) { type = NavType.StringType },
            navArgument(QuestionnairesRoute.ARG_CATEGORY_NAME) {
                type = NavType.StringType
                defaultValue = ""
            },
        ),
    ) {
        QuestionnairesScreenRoute(onBack = onBack, onOpenQuestionnaire = onOpenQuestionnaire)
    }
}
