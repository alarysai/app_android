package com.alarysai.alarysai.core.navigation

import android.net.Uri

/**
 * Shared route contracts. Each feature registers its own destinations
 * (e.g. `NavGraphBuilder.homeDestination`) using the routes declared here,
 * so the app module wires the graph without features depending on each other.
 */
interface AppRoute {
    val route: String
}

/** Bottom bar tabs, left to right. [HomeRoute] is the start destination. */
object HomeRoute : AppRoute {
    override val route: String = "home"
}

object HistoryRoute : AppRoute {
    override val route: String = "history"
}

object PlansRoute : AppRoute {
    override val route: String = "plans"
}

object ClubRoute : AppRoute {
    override val route: String = "club"
}

object ProfileRoute : AppRoute {
    override val route: String = "profile"
}

/**
 * Published questionnaires of one category. The category name travels along so the
 * screen can show its title without reading the category again.
 */
object QuestionnairesRoute : AppRoute {
    const val ARG_CATEGORY_ID = "categoryId"
    const val ARG_CATEGORY_NAME = "categoryName"

    override val route: String = "questionnaires/{$ARG_CATEGORY_ID}?$ARG_CATEGORY_NAME={$ARG_CATEGORY_NAME}"

    fun create(categoryId: String, categoryName: String): String =
        "questionnaires/${Uri.encode(categoryId)}?$ARG_CATEGORY_NAME=${Uri.encode(categoryName)}"
}

/** Runs one questionnaire step by step. The title travels along to show while it loads. */
object QuestionnaireRunRoute : AppRoute {
    const val ARG_QUESTIONNAIRE_ID = "questionnaireId"
    const val ARG_TITLE = "title"

    override val route: String = "questionnaire/{$ARG_QUESTIONNAIRE_ID}?$ARG_TITLE={$ARG_TITLE}"

    fun create(questionnaireId: String, title: String): String =
        "questionnaire/${Uri.encode(questionnaireId)}?$ARG_TITLE=${Uri.encode(title)}"
}
