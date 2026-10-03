package com.alarysai.alarysai.feature.history.presentation.screen

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.alarysai.alarysai.core.navigation.HistoryRoute

/** The "History" tab of the bottom bar. */
fun NavGraphBuilder.historyDestination() {
    composable(route = HistoryRoute.route) {
        HistoryScreenRoute()
    }
}
