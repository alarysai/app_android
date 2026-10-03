package com.alarysai.alarysai.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.alarysai.alarysai.R
import com.alarysai.alarysai.core.navigation.AppRoute
import com.alarysai.alarysai.core.navigation.ClubRoute
import com.alarysai.alarysai.core.navigation.HistoryRoute
import com.alarysai.alarysai.core.navigation.HomeRoute
import com.alarysai.alarysai.core.navigation.PlansRoute
import com.alarysai.alarysai.core.navigation.ProfileRoute

/** Bottom bar tabs, left to right, as in the home mockup. */
enum class TopLevelTab(
    val route: AppRoute,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    HOME(HomeRoute, R.string.tab_home, Icons.Outlined.Home),
    HISTORY(HistoryRoute, R.string.tab_history, Icons.Outlined.History),
    PLANS(PlansRoute, R.string.tab_plans, Icons.Outlined.CreditCard),
    CLUB(ClubRoute, R.string.tab_club, Icons.Outlined.Groups),
    PROFILE(ProfileRoute, R.string.tab_profile, Icons.Outlined.Person),
}
