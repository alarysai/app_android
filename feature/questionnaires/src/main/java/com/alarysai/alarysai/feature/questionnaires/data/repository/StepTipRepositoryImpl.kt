package com.alarysai.alarysai.feature.questionnaires.data.repository

import android.util.Log
import com.alarysai.alarysai.feature.questionnaires.data.mapper.toDomainOrNull
import com.alarysai.alarysai.feature.questionnaires.data.remote.TipRemoteDataSource
import com.alarysai.alarysai.feature.questionnaires.domain.model.Tip
import com.alarysai.alarysai.feature.questionnaires.domain.repository.StepTipRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class StepTipRepositoryImpl @Inject constructor(
    private val remoteDataSource: TipRemoteDataSource,
) : StepTipRepository {

    override suspend fun getActiveTip(tipId: String): Tip? =
        try {
            remoteDataSource.getTip(tipId)?.let { it.data.toDomainOrNull(it.id) }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            // Inactive tips fail with PERMISSION_DENIED by design: the step shows without a tip.
            Log.i(TAG, "No tip to show for $tipId", error)
            null
        }

    private companion object {
        const val TAG = "StepTipRepository"
    }
}
