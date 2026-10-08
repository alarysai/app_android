package com.alarysai.alarysai.feature.tips.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.alarysai.alarysai.core.designsystem.component.AlarysBackground
import com.alarysai.alarysai.core.designsystem.component.GlassCard
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.core.designsystem.theme.Cyan400
import com.alarysai.alarysai.feature.tips.R
import com.alarysai.alarysai.feature.tips.presentation.state.TipItemUi

/** Lines of tip text that fit the carousel height; longer tips end with "…". */
private const val TIP_TEXT_MAX_LINES = 4

/** One tip, sized by the caller (the carousel gives every card the same height). */
@Composable
fun TipCard(
    tip: TipItemUi,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier.fillMaxWidth(), accent = Cyan400) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                if (tip.categoryName != null) {
                    Text(
                        text = tip.categoryName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = tip.text,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = TIP_TEXT_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!tip.isInUserLanguage) PortugueseOnlyBadge()
            }
            if (tip.imageUrl != null) {
                AsyncImage(
                    model = tip.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(10.dp)),
                )
            }
        }
    }
}

@Composable
private fun PortugueseOnlyBadge() {
    Text(
        text = stringResource(R.string.tips_portuguese_only),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@Preview
@Composable
private fun TipCardPreview() {
    AlarysTheme {
        AlarysBackground {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TipCard(TipItemUi("1", "Sempre cite as fontes que a IA usou.", null, "Ética", true), Modifier.height(TipCarouselHeight))
                TipCard(TipItemUi("2", "Revise o texto antes de publicar.", null, null, false), Modifier.height(TipCarouselHeight))
            }
        }
    }
}
