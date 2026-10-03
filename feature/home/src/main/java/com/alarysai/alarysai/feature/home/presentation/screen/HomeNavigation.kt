package com.alarysai.alarysai.feature.home.presentation.screen

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.alarysai.alarysai.core.navigation.HomeRoute

fun NavGraphBuilder.homeDestination(onOpenCategory: (categoryId: String, categoryName: String) -> Unit) {
    composable(route = HomeRoute.route) {
        HomeScreenRoute(onOpenCategory = onOpenCategory)
    }
}
