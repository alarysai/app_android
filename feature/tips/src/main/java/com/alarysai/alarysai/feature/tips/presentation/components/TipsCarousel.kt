package com.alarysai.alarysai.feature.tips.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alarysai.alarysai.core.designsystem.component.AlarysBackground
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme
import com.alarysai.alarysai.feature.tips.R
import com.alarysai.alarysai.feature.tips.presentation.state.TipItemUi
import kotlinx.coroutines.delay

const val TIPS_CAROUSEL_TAG = "tips_carousel"
const val TIPS_CAROUSEL_DOTS_TAG = "tips_carousel_dots"

/** Time each tip stays on screen before the carousel moves to the next one. */
const val TIP_AUTO_ADVANCE_MILLIS = 5_000L

/** Fixed height, so the screen below does not jump when tips of different lengths pass. */
val TipCarouselHeight: Dp = 144.dp

/**
 * Tips one at a time, swiping sideways, endless in both directions. Every
 * [autoAdvanceMillis] it moves to the next tip; any swipe restarts that wait, and the carousel
 * does not move while the user is dragging. Dots below show where the user is.
 */
@Composable
fun TipsCarousel(
    tips: List<TipItemUi>,
    modifier: Modifier = Modifier,
    autoAdvanceMillis: Long = TIP_AUTO_ADVANCE_MILLIS,
) {
    val paging = remember(tips.size) { CarouselPaging(tips.size) }
    val pagerState = rememberPagerState(initialPage = paging.initialPage) { paging.pageCount }
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    // Restarts on every settled page (auto or by hand) and pauses while the user drags.
    LaunchedEffect(pagerState.settledPage, isDragged, paging) {
        if (!paging.isEndless || isDragged) return@LaunchedEffect
        delay(autoAdvanceMillis)
        pagerState.animateScrollToPage(pagerState.settledPage + 1)
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 12.dp,
            modifier = Modifier
                .height(TipCarouselHeight)
                .testTag(TIPS_CAROUSEL_TAG),
        ) { page ->
            TipCard(tip = tips[paging.tipIndex(page)], modifier = Modifier.height(TipCarouselHeight))
        }
        if (paging.isEndless) {
            PageDots(count = tips.size, selected = paging.tipIndex(pagerState.currentPage))
        }
    }
}

/** "There is more to the side": one dot per tip, the current one longer and highlighted. */
@Composable
private fun PageDots(count: Int, selected: Int) {
    val description = stringResource(R.string.tips_carousel_position, selected + 1, count)
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TIPS_CAROUSEL_DOTS_TAG)
            .semantics { contentDescription = description },
    ) {
        repeat(count) { index ->
            val isSelected = index == selected
            val width by animateDpAsState(if (isSelected) 18.dp else 6.dp, label = "dotWidth")
            val color by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                label = "dotColor",
            )
            Box(
                modifier = Modifier
                    .size(height = 6.dp, width = width)
                    .background(color, CircleShape),
            )
        }
    }
}

@Preview
@Composable
private fun TipsCarouselPreview() {
    AlarysTheme {
        AlarysBackground {
            Box(Modifier.padding(vertical = 16.dp)) {
                TipsCarousel(
                    tips = listOf(
                        TipItemUi("1", "Sempre cite as fontes que a IA usou.", null, "Ética", true),
                        TipItemUi("2", "Peça exemplos para entender melhor.", null, "Conhecimento", false),
                        TipItemUi("3", "Revise o texto antes de publicar.", null, null, true),
                    ),
                )
            }
        }
    }
}
