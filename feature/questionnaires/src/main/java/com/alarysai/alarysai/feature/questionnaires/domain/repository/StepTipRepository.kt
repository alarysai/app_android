package com.alarysai.alarysai.feature.questionnaires.domain.repository

import com.alarysai.alarysai.feature.questionnaires.domain.model.Tip

interface StepTipRepository {

    /**
     * The tip linked to a step, or null when there is none to show: deactivated (the rules deny
     * the read), removed, malformed, or unreachable. A tip is secondary content, so it never fails.
     */
    suspend fun getActiveTip(tipId: String): Tip?
}
