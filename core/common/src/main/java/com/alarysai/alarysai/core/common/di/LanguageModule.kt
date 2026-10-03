package com.alarysai.alarysai.core.common.di

import com.alarysai.alarysai.core.common.language.DeviceLanguageProvider
import com.alarysai.alarysai.core.common.language.LanguageProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LanguageModule {

    @Binds
    abstract fun bindsLanguageProvider(impl: DeviceLanguageProvider): LanguageProvider
}
