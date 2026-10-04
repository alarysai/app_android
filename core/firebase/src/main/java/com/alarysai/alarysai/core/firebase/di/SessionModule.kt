package com.alarysai.alarysai.core.firebase.di

import com.alarysai.alarysai.core.common.session.SessionRepository
import com.alarysai.alarysai.core.common.session.UserProfileRepository
import com.alarysai.alarysai.core.firebase.session.FirebaseSessionRepository
import com.alarysai.alarysai.core.firebase.session.FirestoreUserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {

    @Binds
    abstract fun bindsSessionRepository(impl: FirebaseSessionRepository): SessionRepository

    @Binds
    abstract fun bindsUserProfileRepository(impl: FirestoreUserProfileRepository): UserProfileRepository

    companion object {
        @Provides
        @Singleton
        fun providesFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    }
}
