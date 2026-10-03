package com.alarysai.alarysai.feature.questionnaires.data.remote

import com.alarysai.alarysai.core.firebase.document.RemoteDocument
import com.alarysai.alarysai.feature.questionnaires.data.model.TipDto

interface TipRemoteDataSource {
    /** Null when the document does not exist. Throws PERMISSION_DENIED for an inactive tip. */
    suspend fun getTip(tipId: String): RemoteDocument<TipDto>?
}
