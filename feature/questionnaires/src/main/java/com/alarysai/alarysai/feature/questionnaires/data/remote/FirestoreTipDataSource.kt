package com.alarysai.alarysai.feature.questionnaires.data.remote

import com.alarysai.alarysai.core.firebase.document.RemoteDocument
import com.alarysai.alarysai.core.firebase.document.getRemoteDocumentOrNull
import com.alarysai.alarysai.feature.questionnaires.data.model.TipDto
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

/** Single tip read from `android-integration.md` section 4.4. */
class FirestoreTipDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) : TipRemoteDataSource {

    override suspend fun getTip(tipId: String): RemoteDocument<TipDto>? =
        firestore.collection(COLLECTION).document(tipId).getRemoteDocumentOrNull(TipDto::class.java)

    private companion object {
        const val COLLECTION = "tips"
    }
}
