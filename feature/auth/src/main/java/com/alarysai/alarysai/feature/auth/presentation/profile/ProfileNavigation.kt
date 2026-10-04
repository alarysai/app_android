package com.alarysai.alarysai.feature.auth.presentation.profile

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.alarysai.alarysai.core.navigation.ProfileRoute

/** The "Profile" tab: the same screen as the first-access setup, in edit mode and with sign-out. */
fun NavGraphBuilder.profileDestination() {
    composable(
        route = ProfileRoute.route,
        arguments = listOf(
            navArgument(ProfileViewModel.ARG_MODE) {
                type = NavType.StringType
                defaultValue = ProfileViewModel.MODE_EDIT
            },
        ),
    ) {
        ProfileScreenRoute()
    }
}
