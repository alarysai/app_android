package com.alarysai.alarysai.core.firebase.error

import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code

/**
 * See `android-integration.md` section 9.
 *
 * PERMISSION_DENIED on a list query means the query itself is wrong (missing status filter),
 * which is an app bug; it still maps to [ContentLoadError.UNAVAILABLE] so the user sees a
 * calm message instead of a crash.
 */
fun Throwable.toContentLoadError(): ContentLoadError {
    val code = (this as? FirebaseFirestoreException)?.code ?: return ContentLoadError.UNKNOWN
    return when (code) {
        Code.UNAVAILABLE, Code.DEADLINE_EXCEEDED -> ContentLoadError.OFFLINE
        Code.PERMISSION_DENIED, Code.NOT_FOUND -> ContentLoadError.UNAVAILABLE
        else -> ContentLoadError.UNKNOWN
    }
}
