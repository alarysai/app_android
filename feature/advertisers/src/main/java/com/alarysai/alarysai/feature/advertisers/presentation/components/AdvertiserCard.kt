package com.alarysai.alarysai.feature.advertisers.presentation.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.alarysai.alarysai.core.designsystem.component.GlassCard
import com.alarysai.alarysai.core.designsystem.theme.CardAccents
import com.alarysai.alarysai.feature.advertisers.R
import com.alarysai.alarysai.feature.advertisers.presentation.state.AdvertiserItemUi

/** One advertiser; tapping opens its link. Image-only advertisers are labeled with the link's host. */
@Composable
fun AdvertiserCard(
    advertiser: AdvertiserItemUi,
    accentIndex: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = CardAccents[accentIndex.mod(CardAccents.size)]
    val label = advertiser.name ?: Uri.parse(advertiser.link).host ?: advertiser.link
    GlassCard(modifier = modifier.fillMaxWidth(), accent = accent, onClick = onClick) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AdvertiserLogo(advertiser.imageUrl, label, accent)
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = stringResource(R.string.advertisers_open_link),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Logos are always null until uploads are enabled; the initial letter stands in for them. */
@Composable
private fun AdvertiserLogo(imageUrl: String?, label: String, accent: Color) {
    val shape = RoundedCornerShape(10.dp)
    val logoModifier = Modifier
        .size(48.dp)
        .clip(shape)
    if (imageUrl != null) {
        AsyncImage(model = imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = logoModifier)
    } else {
        Box(
            modifier = logoModifier
                .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.9f), accent.copy(alpha = 0.4f))))
                .border(1.dp, accent, shape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label.take(1).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}
