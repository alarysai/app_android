package com.alarysai.alarysai.feature.advertisers.di

import com.alarysai.alarysai.feature.advertisers.data.remote.AdvertisersRemoteDataSource
import com.alarysai.alarysai.feature.advertisers.data.remote.FirestoreAdvertisersDataSource
import com.alarysai.alarysai.feature.advertisers.data.repository.AdvertiserRepositoryImpl
import com.alarysai.alarysai.feature.advertisers.domain.repository.AdvertiserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AdvertisersModule {

    @Binds
    abstract fun bindsAdvertisersRemoteDataSource(impl: FirestoreAdvertisersDataSource): AdvertisersRemoteDataSource

    @Binds
    abstract fun bindsAdvertiserRepository(impl: AdvertiserRepositoryImpl): AdvertiserRepository
}
