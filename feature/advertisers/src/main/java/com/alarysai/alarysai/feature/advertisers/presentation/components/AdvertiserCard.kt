package com.alarysai.alarysai.feature.advertisers.presentation.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.alarysai.alarysai.core.designsystem.component.GlassCard
import com.alarysai.alarysai.core.designsystem.theme.CardAccents
import com.alarysai.alarysai.feature.advertisers.R
import com.alarysai.alarysai.feature.advertisers.presentation.state.AdvertiserItemUi

/** Test tag of the banner image (cards with an image). */
const val ADVERTISER_BANNER_TAG = "advertiser_banner"

/**
 * Height of a card with an image: the text-only card (48 dp initial + 2 × 14 dp padding = 76 dp)
 * plus 25%. The image takes the top half, the name the bottom half.
 */
private val BannerCardHeight = 96.dp

private val ContentPadding = 14.dp

/**
 * One advertiser; tapping opens its link. With an image, the image is a full-width banner on the
 * top half, cropped to fill and clipped by the card's rounded corners; without one, the initial
 * stands in for it. Image-only advertisers are labeled with the link's host.
 */
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
        if (advertiser.imageUrl != null) {
            BannerContent(imageUrl = advertiser.imageUrl, label = label)
        } else {
            Row(
                modifier = Modifier.padding(ContentPadding),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AdvertiserInitial(label, accent)
                AdvertiserLabel(label = label, maxLines = 2, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BannerContent(imageUrl: String, label: String) {
    // The card (GlassCard) clips its content, so the banner follows the rounded top corners.
    Column(modifier = Modifier.fillMaxWidth().height(BannerCardHeight)) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag(ADVERTISER_BANNER_TAG),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = ContentPadding),
            contentAlignment = Alignment.CenterStart,
        ) {
            AdvertiserLabel(label = label, maxLines = 1)
        }
    }
}

/** Name (or host) and the "open link" icon. */
@Composable
private fun AdvertiserLabel(label: String, maxLines: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = maxLines,
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

/** Stand-in for advertisers without an image: the label's initial on the card's accent. */
@Composable
private fun AdvertiserInitial(label: String, accent: Color) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(shape)
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
