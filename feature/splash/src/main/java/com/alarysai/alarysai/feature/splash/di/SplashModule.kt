package com.alarysai.alarysai.feature.splash.di

import android.content.Context
import android.content.res.AssetManager
import com.alarysai.alarysai.feature.splash.data.repository.AssetSplashVideoRepository
import com.alarysai.alarysai.feature.splash.domain.model.SplashTiming
import com.alarysai.alarysai.feature.splash.domain.repository.SplashVideoRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SplashModule {

    @Binds
    abstract fun bindSplashVideoRepository(impl: AssetSplashVideoRepository): SplashVideoRepository

    companion object {
        @Provides
        fun provideAssetManager(@ApplicationContext context: Context): AssetManager = context.assets

        @Provides
        fun provideSplashTiming(): SplashTiming = SplashTiming()
    }
}
