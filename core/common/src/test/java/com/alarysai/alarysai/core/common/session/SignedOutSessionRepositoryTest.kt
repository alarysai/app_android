package com.alarysai.alarysai.core.common.session

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SignedOutSessionRepositoryTest {

    @Test
    fun `nobody is signed in until sign-in exists`() = runTest {
        assertEquals(listOf<String?>(null), SignedOutSessionRepository().observeUserId().toList())
    }
}
