package com.alarysai.alarysai.feature.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.alarysai.alarysai.core.designsystem.theme.Blue500
import com.alarysai.alarysai.core.designsystem.theme.TextPrimary
import com.alarysai.alarysai.core.designsystem.theme.Violet500

/** Round violet/blue mic used inside the search bar. */
@Composable
internal fun MicButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .background(Brush.linearGradient(listOf(Violet500, Blue500)), CircleShape),
    ) {
        Icon(
            imageVector = Icons.Filled.Mic,
            contentDescription = contentDescription,
            tint = TextPrimary,
            modifier = Modifier.size(20.dp),
        )
    }
}
