package com.alarysai.alarysai.feature.questionnaires.data.repository

import com.alarysai.alarysai.core.firebase.document.RemoteDocument
import com.alarysai.alarysai.core.firebase.model.LocalizedTextDto
import com.alarysai.alarysai.feature.questionnaires.data.model.TipDto
import com.alarysai.alarysai.feature.questionnaires.data.remote.TipRemoteDataSource
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StepTipRepositoryImplTest {

    private class FakeDataSource(private val result: suspend () -> RemoteDocument<TipDto>?) : TipRemoteDataSource {
        override suspend fun getTip(tipId: String) = result()
    }

    private fun repository(result: suspend () -> RemoteDocument<TipDto>?) = StepTipRepositoryImpl(FakeDataSource(result))

    @Test
    fun `returns an active tip`() = runTest {
        val tip = repository { RemoteDocument("tip1", TipDto(text = LocalizedTextDto(pt = "Cite as fontes."), status = "active")) }
            .getActiveTip("tip1")

        assertEquals("Cite as fontes.", tip?.text?.pt)
    }

    @Test
    fun `deactivated tip (permission denied) is no tip, without failing`() = runTest {
        val tip = repository {
            throw FirebaseFirestoreException("denied", FirebaseFirestoreException.Code.PERMISSION_DENIED)
        }.getActiveTip("tip1")

        assertNull(tip)
    }

    @Test
    fun `missing, inactive or unreachable tip is no tip`() = runTest {
        assertNull(repository { null }.getActiveTip("tip1"))
        assertNull(repository { RemoteDocument("tip1", TipDto(text = LocalizedTextDto(pt = "x"), status = "inactive")) }.getActiveTip("tip1"))
        assertNull(
            repository { throw FirebaseFirestoreException("offline", FirebaseFirestoreException.Code.UNAVAILABLE) }
                .getActiveTip("tip1"),
        )
    }
}
