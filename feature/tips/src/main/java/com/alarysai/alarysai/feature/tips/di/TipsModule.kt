package com.alarysai.alarysai.feature.tips.di

import com.alarysai.alarysai.feature.tips.data.remote.FirestoreTipsDataSource
import com.alarysai.alarysai.feature.tips.data.remote.TipsRemoteDataSource
import com.alarysai.alarysai.feature.tips.data.repository.TipRepositoryImpl
import com.alarysai.alarysai.feature.tips.domain.repository.TipRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TipsModule {

    @Binds
    abstract fun bindsTipsRemoteDataSource(impl: FirestoreTipsDataSource): TipsRemoteDataSource

    @Binds
    abstract fun bindsTipRepository(impl: TipRepositoryImpl): TipRepository
}
