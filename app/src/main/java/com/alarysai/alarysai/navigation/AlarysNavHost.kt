package com.alarysai.alarysai.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alarysai.alarysai.core.designsystem.component.AlarysBackground
import com.alarysai.alarysai.core.designsystem.component.AlarysBottomBar
import com.alarysai.alarysai.core.designsystem.component.BottomBarItem
import com.alarysai.alarysai.core.navigation.ClubRoute
import com.alarysai.alarysai.core.navigation.HistoryRoute
import com.alarysai.alarysai.core.navigation.HomeRoute
import com.alarysai.alarysai.core.navigation.QuestionnaireRunRoute
import com.alarysai.alarysai.core.navigation.QuestionnairesRoute
import com.alarysai.alarysai.feature.history.presentation.screen.historyDestination
import com.alarysai.alarysai.feature.home.presentation.screen.homeDestination
import com.alarysai.alarysai.feature.questionnaires.presentation.run.screen.questionnaireRunDestination
import com.alarysai.alarysai.feature.questionnaires.presentation.screen.questionnairesDestination

/** Root of the app: brand background, bottom bar and the tab destinations (Club AI shows tips and advertisers). */
@Composable
fun AlarysNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val tabRoutes = TopLevelTab.entries.map { it.route.route }
    // Screens opened from a tab (e.g. a category's questionnaires) keep Home highlighted.
    val selectedTabRoute = currentRoute?.takeIf { it in tabRoutes } ?: HomeRoute.route
    val items = TopLevelTab.entries.map { tab ->
        BottomBarItem(key = tab.route.route, label = stringResource(tab.labelRes), icon = tab.icon)
    }

    AlarysBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                // The questionnaire run is full screen, with its own bottom actions.
                if (currentRoute != QuestionnaireRunRoute.route) {
                    AlarysBottomBar(
                        items = items,
                        selectedKey = selectedTabRoute,
                        onItemClick = { navController.navigateToTab(it.key) },
                    )
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = HomeRoute.route,
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            ) {
                homeDestination(
                    onOpenCategory = { categoryId, categoryName ->
                        navController.navigate(QuestionnairesRoute.create(categoryId, categoryName))
                    },
                )
                questionnairesDestination(
                    onBack = { navController.popBackStack() },
                    onOpenQuestionnaire = { questionnaireId, title ->
                        navController.navigate(QuestionnaireRunRoute.create(questionnaireId, title))
                    },
                )
                questionnaireRunDestination(onExit = { navController.popBackStack() })
                clubDestination()
                historyDestination()
                // Tabs whose feature does not exist yet.
                TopLevelTab.entries
                    .filter { it.route !in setOf(HomeRoute, ClubRoute, HistoryRoute) }
                    .forEach { tab ->
                        composable(tab.route.route) { ComingSoonScreen(title = stringResource(tab.labelRes)) }
                    }
            }
        }
    }
}

/** Standard tab switching: one copy of each tab, state restored when coming back. */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
