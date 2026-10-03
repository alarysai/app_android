package com.alarysai.alarysai.feature.history.presentation.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.alarysai.alarysai.feature.history.R
import com.alarysai.alarysai.feature.history.domain.model.CreditKind
import com.alarysai.alarysai.feature.history.domain.model.OutputType
import java.text.DateFormat
import java.util.Date

@StringRes
internal fun OutputType.labelRes(): Int = when (this) {
    OutputType.TEXT -> R.string.history_output_text
    OutputType.IMAGE -> R.string.history_output_image
    OutputType.VIDEO -> R.string.history_output_video
    OutputType.SLIDES -> R.string.history_output_slides
    OutputType.OTHER -> R.string.history_output_other
}

@StringRes
internal fun CreditKind.labelRes(): Int = when (this) {
    CreditKind.PURCHASE -> R.string.history_credit_purchase
    CreditKind.USAGE -> R.string.history_credit_usage
    CreditKind.MONTHLY_GRANT -> R.string.history_credit_monthly_grant
    CreditKind.BONUS -> R.string.history_credit_bonus
    CreditKind.REFUND -> R.string.history_credit_refund
    CreditKind.OTHER -> R.string.history_credit_other
}

/** Date and time in the device locale; null while the server timestamp is pending. */
@Composable
internal fun rememberFormattedDate(millis: Long?): String? = remember(millis) {
    millis?.let { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(it)) }
}
