package com.alarysai.alarysai.feature.home.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.alarysai.alarysai.feature.home.presentation.state.CategoryItemUi

const val CATEGORY_GRID_TAG = "category_grid"
private const val CARDS_PER_ROW = 3

/**
 * Rows of three cards; a shorter last row stretches to the full width, so five categories
 * render as 3 + 2 like the mockup. Not lazy: the list is short and sits inside a scrolling screen.
 */
@Composable
fun CategoryGrid(
    categories: List<CategoryItemUi>,
    onCategoryClick: (CategoryItemUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(CATEGORY_GRID_TAG),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        categories.chunked(CARDS_PER_ROW).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { category ->
                    CategoryCard(
                        category = category,
                        onClick = { onCategoryClick(category) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
}
