package com.alarysai.alarysai.feature.plans.presentation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.alarysai.alarysai.core.navigation.PlansRoute

/** The "Plans" tab of the bottom bar. */
fun NavGraphBuilder.plansDestination() {
    composable(route = PlansRoute.route) {
        PlansScreenRoute()
    }
}
