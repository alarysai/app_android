package com.alarysai.alarysai.core.firebase.document

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/** A Firestore document converted to its DTO. The document ID is not a field, so it travels apart. */
data class RemoteDocument<T>(
    val id: String,
    val data: T,
)

data class RemoteDocumentList<T>(
    val documents: List<RemoteDocument<T>>,
    val isFromCache: Boolean,
)

private const val TAG = "RemoteDocuments"

/**
 * Converts a document without ever throwing: a malformed document (e.g. a field with the
 * wrong type) is skipped and logged, so one bad entry never breaks the whole screen.
 */
fun <T : Any> DocumentSnapshot.toRemoteDocumentOrNull(type: Class<T>): RemoteDocument<T>? =
    runCatching { toObject(type) }
        .onFailure { Log.w(TAG, "Skipping malformed document ${reference.path}", it) }
        .getOrNull()
        ?.let { RemoteDocument(id, it) }

/**
 * Live query results; emits again when the server or the offline cache changes.
 *
 * Metadata changes are included on purpose: when the server confirms the cached data
 * unchanged, only `isFromCache` flips to false, and without them the listener would never
 * report it (the screen would keep showing the offline notice while online).
 */
fun <T : Any> Query.observeDocuments(type: Class<T>): Flow<RemoteDocumentList<T>> =
    snapshots(MetadataChanges.INCLUDE).map { snapshot ->
        RemoteDocumentList(
            documents = snapshot.documents.mapNotNull { it.toRemoteDocumentOrNull(type) },
            isFromCache = snapshot.metadata.isFromCache,
        )
    }

/** One-shot read; null when the document does not exist. Throws `FirebaseFirestoreException` on failure. */
suspend fun <T : Any> DocumentReference.getRemoteDocumentOrNull(type: Class<T>): RemoteDocument<T>? {
    val snapshot = get().await()
    return if (snapshot.exists()) snapshot.toRemoteDocumentOrNull(type) else null
}

/** One-shot query read, skipping malformed documents. Throws `FirebaseFirestoreException` on failure. */
suspend fun <T : Any> Query.getRemoteDocuments(type: Class<T>): List<RemoteDocument<T>> =
    get().await().documents.mapNotNull { it.toRemoteDocumentOrNull(type) }

/**
 * One live document; emits null while it does not exist. Includes metadata changes for the
 * same reason as [observeDocuments]. A malformed document is also reported as null.
 */
fun <T : Any> DocumentReference.observeRemoteDocument(type: Class<T>): Flow<RemoteDocument<T>?> =
    snapshots(MetadataChanges.INCLUDE).map { snapshot ->
        if (snapshot.exists()) snapshot.toRemoteDocumentOrNull(type) else null
    }

/** Deletes the document. Throws `FirebaseFirestoreException` on failure (e.g. denied by the rules). */
suspend fun DocumentReference.deleteDocument() {
    delete().await()
}
