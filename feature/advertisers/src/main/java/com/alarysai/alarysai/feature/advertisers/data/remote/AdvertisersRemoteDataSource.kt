package com.alarysai.alarysai.feature.advertisers.data.remote

import com.alarysai.alarysai.core.firebase.FirestoreContract
import com.alarysai.alarysai.core.firebase.document.RemoteDocumentList
import com.alarysai.alarysai.core.firebase.document.observeDocuments
import com.alarysai.alarysai.feature.advertisers.data.model.AdvertiserDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface AdvertisersRemoteDataSource {
    fun observeActiveAdvertisers(): Flow<RemoteDocumentList<AdvertiserDto>>
}

/**
 * Query from `android-integration.md` section 4.5, backed by the composite index
 * `advertisers (status, order)`. Keep the status filter: the rules reject the query without it.
 */
class FirestoreAdvertisersDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) : AdvertisersRemoteDataSource {

    override fun observeActiveAdvertisers(): Flow<RemoteDocumentList<AdvertiserDto>> =
        firestore.collection(COLLECTION)
            .whereEqualTo(FirestoreContract.FIELD_STATUS, FirestoreContract.STATUS_ACTIVE)
            .orderBy(FirestoreContract.FIELD_ORDER, Query.Direction.ASCENDING)
            .observeDocuments(AdvertiserDto::class.java)

    private companion object {
        const val COLLECTION = "advertisers"
    }
}
