package com.alarysai.alarysai.feature.plans.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.feature.plans.domain.model.BillingPeriod
import com.alarysai.alarysai.feature.plans.domain.repository.PurchaseHost
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Strings are asserted in Portuguese: run on a device whose language is pt. */
@RunWith(AndroidJUnit4::class)
class PlansScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val host = object : PurchaseHost {}
    private val current = PlanUi("basico", "Básico", "40 créditos por mês", "R$ 19,90", BillingPeriod.MONTH, "t1", isCurrent = true)
    private val pro = PlanUi("pro", "Pro", "150 créditos por mês", "R$ 49,90", BillingPeriod.MONTH, "t2", isCurrent = false)
    private val pack = CreditPackUi("c50", "50 créditos", "Use quando quiser", "R$ 14,90")

    private fun setScreen(enabled: Boolean, onAction: (PlansUiAction) -> Unit = {}) {
        composeRule.setContent {
            AlarysTheme {
                PlansScreen(
                    uiState = PlansUiState(content = PlansContent.Ready(listOf(current, pro), listOf(pack)), purchasesEnabled = enabled),
                    onAction = onAction,
                    host = host,
                )
            }
        }
    }

    @Test
    fun showsPlansPricesCurrentPlanAndBuys() {
        val actions = mutableListOf<PlansUiAction>()
        setScreen(enabled = true) { actions += it }

        composeRule.onNodeWithText("Seu plano").assertIsDisplayed()
        composeRule.onNodeWithText("R$ 49,90 por mês").assertIsDisplayed()
        composeRule.onNodeWithText("Assinar").performClick()
        composeRule.onNodeWithText("Comprar").performClick()

        assertEquals(listOf(PlansUiAction.SubscribeClicked(pro, host), PlansUiAction.BuyCreditsClicked(pack, host)), actions)
    }

    @Test
    fun releaseShowsComingSoon() {
        setScreen(enabled = false)

        composeRule.onNodeWithText("As compras pelo app chegam em breve. Por enquanto, veja os planos disponíveis.").assertIsDisplayed()
        composeRule.onNodeWithText("Comprar", useUnmergedTree = true).assertDoesNotExist()
    }
}
