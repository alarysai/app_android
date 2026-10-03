package com.alarysai.alarysai.core.firebase.di

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {

    /** Offline persistence is on by default, so content opens without network. */
    @Provides
    @Singleton
    fun providesFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}
