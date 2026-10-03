package com.alarysai.alarysai.feature.questionnaires.di

import com.alarysai.alarysai.feature.questionnaires.data.remote.FirestoreQuestionnaireDataSource
import com.alarysai.alarysai.feature.questionnaires.data.remote.FirestoreTipDataSource
import com.alarysai.alarysai.feature.questionnaires.data.remote.QuestionnaireRemoteDataSource
import com.alarysai.alarysai.feature.questionnaires.data.remote.TipRemoteDataSource
import com.alarysai.alarysai.feature.questionnaires.data.repository.QuestionnaireRepositoryImpl
import com.alarysai.alarysai.feature.questionnaires.data.repository.StepTipRepositoryImpl
import com.alarysai.alarysai.feature.questionnaires.domain.repository.QuestionnaireRepository
import com.alarysai.alarysai.feature.questionnaires.domain.repository.StepTipRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class QuestionnairesModule {

    @Binds
    abstract fun bindsQuestionnaireRemoteDataSource(
        impl: FirestoreQuestionnaireDataSource,
    ): QuestionnaireRemoteDataSource

    @Binds
    abstract fun bindsQuestionnaireRepository(impl: QuestionnaireRepositoryImpl): QuestionnaireRepository

    @Binds
    abstract fun bindsTipRemoteDataSource(impl: FirestoreTipDataSource): TipRemoteDataSource

    @Binds
    abstract fun bindsStepTipRepository(impl: StepTipRepositoryImpl): StepTipRepository
}
