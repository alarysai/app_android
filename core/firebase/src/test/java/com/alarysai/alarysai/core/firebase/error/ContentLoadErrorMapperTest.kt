package com.alarysai.alarysai.core.firebase.error

import com.alarysai.alarysai.core.common.content.ContentLoadError
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code
import org.junit.Assert.assertEquals
import org.junit.Test

class ContentLoadErrorMapperTest {

    private fun firestoreError(code: Code) = FirebaseFirestoreException("test", code)

    @Test
    fun `network failures map to offline`() {
        assertEquals(ContentLoadError.OFFLINE, firestoreError(Code.UNAVAILABLE).toContentLoadError())
        assertEquals(ContentLoadError.OFFLINE, firestoreError(Code.DEADLINE_EXCEEDED).toContentLoadError())
    }

    @Test
    fun `denied or missing content maps to unavailable`() {
        assertEquals(ContentLoadError.UNAVAILABLE, firestoreError(Code.PERMISSION_DENIED).toContentLoadError())
        assertEquals(ContentLoadError.UNAVAILABLE, firestoreError(Code.NOT_FOUND).toContentLoadError())
    }

    @Test
    fun `other firestore codes and non firestore errors map to unknown`() {
        assertEquals(ContentLoadError.UNKNOWN, firestoreError(Code.FAILED_PRECONDITION).toContentLoadError())
        assertEquals(ContentLoadError.UNKNOWN, IllegalStateException().toContentLoadError())
    }
}
