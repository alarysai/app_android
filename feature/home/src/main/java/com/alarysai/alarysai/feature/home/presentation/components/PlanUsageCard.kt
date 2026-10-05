package com.alarysai.alarysai.feature.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alarysai.alarysai.core.designsystem.component.AlarysBackground
import com.alarysai.alarysai.core.designsystem.component.GlassCard
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.core.designsystem.theme.Blue500
import com.alarysai.alarysai.core.designsystem.theme.Cyan400
import com.alarysai.alarysai.feature.home.R
import com.alarysai.alarysai.feature.home.presentation.state.PlanUsageUi

/** Monthly credit usage. With [usage] null (no active plan data from the server yet) it explains where credits will show up. */
@Composable
fun PlanUsageCard(
    usage: PlanUsageUi?,
    onBuyCreditsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier.fillMaxWidth(), accent = Blue500) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_plan_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (usage != null) {
                    PlanUsageProgress(usage)
                } else {
                    Text(
                        text = stringResource(R.string.home_plan_unavailable),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color.White.copy(alpha = 0.15f)),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (usage != null) {
                    Text(
                        text = pluralStringResource(R.plurals.home_plan_credits, usage.availableCredits, usage.availableCredits),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(R.string.home_plan_credits_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedButton(
                    onClick = onBuyCreditsClick,
                    modifier = Modifier.padding(top = 6.dp),
                ) {
                    Icon(Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(
                        text = stringResource(R.string.home_plan_buy),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanUsageProgress(usage: PlanUsageUi) {
    val usedPercent = usage.usedPercent.coerceIn(0, 100)
    Text(
        text = stringResource(R.string.home_plan_subtitle),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(5.dp)),
    ) {
        if (usedPercent > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(usedPercent / 100f)
                    .height(10.dp)
                    .background(Brush.horizontalGradient(listOf(Blue500, Cyan400)), RoundedCornerShape(5.dp)),
            )
        }
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = stringResource(R.string.home_plan_used, usedPercent),
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = stringResource(R.string.home_plan_available, 100 - usedPercent),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun PlanUsageCardPreview() {
    AlarysTheme {
        AlarysBackground {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PlanUsageCard(usage = PlanUsageUi(usedPercent = 60, availableCredits = 40), onBuyCreditsClick = {})
                PlanUsageCard(usage = null, onBuyCreditsClick = {})
            }
        }
    }
}
