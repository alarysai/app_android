package com.alarysai.alarysai.feature.auth.di

import com.alarysai.alarysai.feature.auth.data.repository.FirebaseAuthRepository
import com.alarysai.alarysai.feature.auth.data.repository.SharedPrefsKeepSignedInRepository
import com.alarysai.alarysai.feature.auth.domain.repository.AuthRepository
import com.alarysai.alarysai.feature.auth.domain.repository.KeepSignedInRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    abstract fun bindsAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds
    abstract fun bindsKeepSignedInRepository(impl: SharedPrefsKeepSignedInRepository): KeepSignedInRepository
}

/**
 * OAuth web client used as `serverClientId` for Google sign-in. Provided by the app module,
 * which reads `default_web_client_id` generated from `google-services.json`.
 */
data class GoogleSignInConfig(val webClientId: String)
