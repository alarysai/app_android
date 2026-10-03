package com.alarysai.alarysai.feature.home.di

import com.alarysai.alarysai.feature.home.data.remote.FirestoreQuestionnaireCategoryDataSource
import com.alarysai.alarysai.feature.home.data.remote.QuestionnaireCategoryRemoteDataSource
import com.alarysai.alarysai.feature.home.data.repository.QuestionnaireCategoryRepositoryImpl
import com.alarysai.alarysai.feature.home.domain.repository.QuestionnaireCategoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeModule {

    @Binds
    abstract fun bindsCategoryRemoteDataSource(
        impl: FirestoreQuestionnaireCategoryDataSource,
    ): QuestionnaireCategoryRemoteDataSource

    @Binds
    abstract fun bindsCategoryRepository(
        impl: QuestionnaireCategoryRepositoryImpl,
    ): QuestionnaireCategoryRepository
}
