package com.alarysai.alarysai.feature.history.di

import com.alarysai.alarysai.feature.history.data.remote.FirestoreUserActivityDataSource
import com.alarysai.alarysai.feature.history.data.remote.UserActivityRemoteDataSource
import com.alarysai.alarysai.feature.history.data.repository.CreditsRepositoryImpl
import com.alarysai.alarysai.feature.history.data.repository.HistoryRepositoryImpl
import com.alarysai.alarysai.feature.history.domain.repository.CreditsRepository
import com.alarysai.alarysai.feature.history.domain.repository.HistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class HistoryModule {

    @Binds
    abstract fun bindsUserActivityRemoteDataSource(impl: FirestoreUserActivityDataSource): UserActivityRemoteDataSource

    @Binds
    abstract fun bindsHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository

    @Binds
    abstract fun bindsCreditsRepository(impl: CreditsRepositoryImpl): CreditsRepository
}
