package com.alarysai.alarysai.feature.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.alarysai.alarysai.core.designsystem.component.GlassCard
import com.alarysai.alarysai.core.designsystem.theme.CardAccents
import com.alarysai.alarysai.feature.home.presentation.state.CategoryItemUi

/** Glass card per category, tinted by its position. */
@Composable
fun CategoryCard(
    category: CategoryItemUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = CardAccents[category.accentIndex.mod(CardAccents.size)]
    GlassCard(modifier = modifier.heightIn(min = 112.dp), accent = accent, onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryIcon(category = category, accent = accent)
                Box(Modifier.weight(1f))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Icons are always null until uploads are enabled; the initial letter stands in for them. */
@Composable
private fun CategoryIcon(category: CategoryItemUi, accent: Color) {
    val shape = RoundedCornerShape(10.dp)
    val iconModifier = Modifier
        .size(44.dp)
        .clip(shape)
    if (category.iconUrl != null) {
        AsyncImage(
            model = category.iconUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = iconModifier,
        )
    } else {
        Box(
            modifier = iconModifier
                .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.9f), accent.copy(alpha = 0.4f))))
                .border(1.dp, accent, shape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = category.name.take(1).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}
