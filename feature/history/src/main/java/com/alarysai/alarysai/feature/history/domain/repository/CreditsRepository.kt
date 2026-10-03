package com.alarysai.alarysai.feature.history.domain.repository

import com.alarysai.alarysai.core.common.content.ContentList
import com.alarysai.alarysai.feature.history.domain.model.CreditTransaction
import kotlinx.coroutines.flow.Flow

interface CreditsRepository {

    /** `users/{uid}.creditBalance`; missing (never credited) counts as 0. Fails with `ContentLoadException`. */
    fun observeBalance(userId: String): Flow<Int>

    /** The credit statement, most recent first. Fails with `ContentLoadException`. */
    fun observeTransactions(userId: String): Flow<ContentList<CreditTransaction>>
}
