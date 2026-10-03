package com.alarysai.alarysai.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.alarysai.alarysai.R
import com.alarysai.alarysai.core.navigation.ClubRoute
import com.alarysai.alarysai.feature.advertisers.presentation.screen.AdvertisersScreenRoute
import com.alarysai.alarysai.feature.tips.presentation.screen.TipsScreenRoute

/** Sections of the "Club AI" tab. Each comes from its own feature module. */
private enum class ClubSection(val labelRes: Int) {
    TIPS(R.string.club_tips),
    ADVERTISERS(R.string.club_advertisers),
}

/**
 * The "Club AI" tab: tips and advertisers side by side. It lives in the app module because it
 * combines two features, which must not depend on each other.
 */
fun NavGraphBuilder.clubDestination() {
    composable(ClubRoute.route) { ClubScreen() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClubScreen() {
    var selected by rememberSaveable { mutableStateOf(ClubSection.TIPS) }
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.tab_club)) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )
        TabRow(selectedTabIndex = selected.ordinal, containerColor = Color.Transparent) {
            ClubSection.entries.forEach { section ->
                Tab(
                    selected = section == selected,
                    onClick = { selected = section },
                    text = { Text(stringResource(section.labelRes)) },
                )
            }
        }
        when (selected) {
            ClubSection.TIPS -> TipsScreenRoute()
            ClubSection.ADVERTISERS -> AdvertisersScreenRoute()
        }
    }
}
