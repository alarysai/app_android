package com.alarysai.alarysai.feature.tips.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alarysai.alarysai.feature.tips.R
import com.alarysai.alarysai.feature.tips.presentation.state.TipCategoryUi

const val TIP_CATEGORY_CHIPS_TAG = "tip_category_chips"

/** "All" followed by each active category. [selectedId] null means "All". */
@Composable
fun TipCategoryChips(
    categories: List<TipCategoryUi>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.testTag(TIP_CATEGORY_CHIPS_TAG),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "all") {
            FilterChip(
                selected = selectedId == null,
                onClick = { onSelect(null) },
                label = { Text(stringResource(R.string.tips_all)) },
            )
        }
        items(categories, key = { it.id }) { category ->
            FilterChip(
                selected = selectedId == category.id,
                onClick = { onSelect(category.id) },
                label = { Text(category.name) },
            )
        }
    }
}
