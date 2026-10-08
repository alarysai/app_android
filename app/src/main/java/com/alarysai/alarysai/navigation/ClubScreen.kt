package com.alarysai.alarysai.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.alarysai.alarysai.R
import com.alarysai.alarysai.core.navigation.ClubRoute
import com.alarysai.alarysai.feature.advertisers.presentation.screen.AdvertisersScreenRoute
import com.alarysai.alarysai.feature.tips.presentation.screen.TipsScreenRoute

/**
 * The "Club AI" tab: the tips carousel on top and the advertisers below, filling the rest. It
 * lives in the app module because it combines two features, which must not depend on each other.
 */
fun NavGraphBuilder.clubDestination() {
    composable(ClubRoute.route) { ClubScreen() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClubScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.tab_club)) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )
        TipsScreenRoute()
        AdvertisersScreenRoute(modifier = Modifier.weight(1f))
    }
}
