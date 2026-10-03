package com.alarysai.alarysai.feature.home.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alarysai.alarysai.core.designsystem.component.AlarysLogo
import com.alarysai.alarysai.feature.home.R

/** Logo centered, notifications bell at the top end. No unread dot until notifications exist. */
@Composable
fun HomeHeader(
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        AlarysLogo(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp),
        )
        IconButton(
            onClick = onNotificationsClick,
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = stringResource(R.string.home_notifications),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
