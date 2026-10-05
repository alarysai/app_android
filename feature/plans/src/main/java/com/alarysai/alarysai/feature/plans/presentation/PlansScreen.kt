package com.alarysai.alarysai.feature.plans.presentation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.alarysai.alarysai.core.designsystem.component.AlarysBackground
import com.alarysai.alarysai.core.designsystem.component.GlassCard
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.core.designsystem.theme.Blue500
import com.alarysai.alarysai.core.designsystem.theme.Teal400
import com.alarysai.alarysai.core.designsystem.theme.Violet500
import com.alarysai.alarysai.core.ui.state.ContentLoadErrorContent
import com.alarysai.alarysai.core.ui.state.LoadingContent
import com.alarysai.alarysai.core.ui.state.MessageContent
import com.alarysai.alarysai.feature.plans.R
import com.alarysai.alarysai.feature.plans.data.billing.ActivityPurchaseHost
import com.alarysai.alarysai.feature.plans.domain.model.BillingError
import com.alarysai.alarysai.feature.plans.domain.model.BillingPeriod
import com.alarysai.alarysai.feature.plans.domain.repository.PurchaseHost

private const val MANAGE_SUBSCRIPTIONS_URL = "https://play.google.com/store/account/subscriptions"

@Composable
fun PlansScreenRoute(viewModel: PlansViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PlansUiEvent.ShowMessage -> snackbarHostState.showSnackbar(context.getString(event.message.textRes()))
                PlansUiEvent.OpenManageSubscriptions -> runCatching { uriHandler.openUri(MANAGE_SUBSCRIPTIONS_URL) }
            }
        }
    }
    val host = remember(context) { context.findActivity()?.let(::ActivityPurchaseHost) }
    PlansScreen(uiState = uiState, onAction = viewModel::onAction, host = host, snackbarHostState = snackbarHostState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    uiState: PlansUiState,
    onAction: (PlansUiAction) -> Unit,
    host: PurchaseHost?,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.plans_title)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { innerPadding ->
        val fill = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when (val content = uiState.content) {
            PlansContent.Loading -> LoadingContent(fill)
            PlansContent.StoreUnavailable -> MessageContent(
                title = stringResource(R.string.plans_store_unavailable_title),
                body = stringResource(R.string.plans_store_unavailable_body),
                modifier = fill,
            )
            PlansContent.Empty -> MessageContent(
                title = stringResource(R.string.plans_empty_title),
                body = stringResource(R.string.plans_empty_body),
                modifier = fill,
            )
            is PlansContent.Error -> ContentLoadErrorContent(
                error = if (content.error == BillingError.NETWORK) ContentLoadError.OFFLINE else ContentLoadError.UNKNOWN,
                onRetry = { onAction(PlansUiAction.Retry) },
                modifier = fill,
            )
            is PlansContent.Ready -> Catalog(content, uiState, onAction, host, fill)
        }
    }
}

@Composable
private fun Catalog(
    content: PlansContent.Ready,
    uiState: PlansUiState,
    onAction: (PlansUiAction) -> Unit,
    host: PurchaseHost?,
    modifier: Modifier,
) {
    val canBuy = uiState.purchasesEnabled && host != null
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!uiState.purchasesEnabled) item { ComingSoonNotice() }
        if (content.plans.isNotEmpty()) {
            item { SectionTitle(stringResource(R.string.plans_subscriptions)) }
            items(content.plans, key = { it.productId }) { plan ->
                PlanCard(
                    plan = plan,
                    canBuy = canBuy,
                    isPurchasing = uiState.purchasingProductId == plan.productId,
                    onSubscribe = { host?.let { onAction(PlansUiAction.SubscribeClicked(plan, it)) } },
                )
            }
        }
        if (content.creditPacks.isNotEmpty()) {
            item { SectionTitle(stringResource(R.string.plans_credit_packs)) }
            items(content.creditPacks, key = { it.productId }) { pack ->
                CreditPackCard(
                    pack = pack,
                    canBuy = canBuy,
                    isPurchasing = uiState.purchasingProductId == pack.productId,
                    onBuy = { host?.let { onAction(PlansUiAction.BuyCreditsClicked(pack, it)) } },
                )
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    stringResource(R.string.plans_renewal_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = { onAction(PlansUiAction.ManageSubscriptionsClicked) }) {
                    Text(stringResource(R.string.plans_manage_subscriptions))
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun ComingSoonNotice() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Violet500.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.plans_coming_soon_notice), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PlanCard(plan: PlanUi, canBuy: Boolean, isPurchasing: Boolean, onSubscribe: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth(), accent = if (plan.isCurrent) Teal400 else Violet500) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(plan.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (plan.isCurrent) CurrentPlanChip()
            }
            if (plan.description.isNotBlank()) {
                Text(plan.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = stringResource(plan.period.priceRes(), plan.price),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            if (!plan.isCurrent) {
                BuyButton(
                    label = stringResource(if (canBuy) R.string.plans_subscribe else R.string.plans_coming_soon),
                    enabled = canBuy,
                    isLoading = isPurchasing,
                    onClick = onSubscribe,
                    filled = true,
                )
            }
        }
    }
}

@Composable
private fun CreditPackCard(pack: CreditPackUi, canBuy: Boolean, isPurchasing: Boolean, onBuy: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth(), accent = Blue500) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(pack.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (pack.description.isNotBlank()) {
                    Text(pack.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(pack.price, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            BuyButton(
                label = stringResource(if (canBuy) R.string.plans_buy else R.string.plans_coming_soon),
                enabled = canBuy,
                isLoading = isPurchasing,
                onClick = onBuy,
                filled = false,
            )
        }
    }
}

@Composable
private fun BuyButton(label: String, enabled: Boolean, isLoading: Boolean, onClick: () -> Unit, filled: Boolean) {
    val content: @Composable () -> Unit = {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) else Text(label, fontWeight = FontWeight.SemiBold)
    }
    if (filled) {
        Button(onClick = onClick, enabled = enabled && !isLoading, modifier = Modifier.fillMaxWidth()) { content() }
    } else {
        OutlinedButton(onClick = onClick, enabled = enabled && !isLoading) { content() }
    }
}

@Composable
private fun CurrentPlanChip() {
    Box(
        modifier = Modifier
            .background(Teal400.copy(alpha = 0.16f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(stringResource(R.string.plans_current), style = MaterialTheme.typography.labelMedium, color = Teal400)
    }
}

private fun BillingPeriod.priceRes(): Int = when (this) {
    BillingPeriod.WEEK -> R.string.plans_price_week
    BillingPeriod.MONTH -> R.string.plans_price_month
    BillingPeriod.QUARTER -> R.string.plans_price_quarter
    BillingPeriod.SEMESTER -> R.string.plans_price_semester
    BillingPeriod.YEAR -> R.string.plans_price_year
    BillingPeriod.OTHER -> R.string.plans_price_other
}

private fun PlansMessage.textRes(): Int = when (this) {
    PlansMessage.DELIVERED -> R.string.plans_message_delivered
    PlansMessage.PENDING -> R.string.plans_message_pending
    PlansMessage.AWAITING_SERVER -> R.string.plans_message_awaiting_server
    PlansMessage.REJECTED -> R.string.plans_message_rejected
    PlansMessage.ALREADY_OWNED -> R.string.plans_message_already_owned
    PlansMessage.STORE_UNAVAILABLE -> R.string.plans_message_store_unavailable
    PlansMessage.NETWORK -> R.string.plans_message_network
    PlansMessage.UNKNOWN -> R.string.plans_message_unknown
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Preview(heightDp = 760)
@Composable
private fun PlansScreenPreview() {
    AlarysTheme {
        AlarysBackground {
            PlansScreen(
                uiState = PlansUiState(
                    purchasesEnabled = false,
                    content = PlansContent.Ready(
                        plans = listOf(
                            PlanUi("basico", "Básico", "40 créditos por mês", "R$ 19,90", BillingPeriod.MONTH, "t1", isCurrent = true),
                            PlanUi("pro", "Pro", "150 créditos por mês", "R$ 49,90", BillingPeriod.MONTH, "t2", isCurrent = false),
                        ),
                        creditPacks = listOf(CreditPackUi("c50", "50 créditos", "Use quando quiser", "R$ 14,90")),
                    ),
                ),
                onAction = {},
                host = null,
            )
        }
    }
}
