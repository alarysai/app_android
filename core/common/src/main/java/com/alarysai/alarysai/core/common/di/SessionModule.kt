package com.alarysai.alarysai.core.common.di

import com.alarysai.alarysai.core.common.session.SessionRepository
import com.alarysai.alarysai.core.common.session.SignedOutSessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {

    /** Replaced by the Firebase Auth session in Task 9. */
    @Binds
    abstract fun bindsSessionRepository(impl: SignedOutSessionRepository): SessionRepository
}
