package com.taras.pet.sharehubbroadcastcenter.data.di

import com.taras.pet.sharehubbroadcastcenter.data.repository.ShareRepositoryImpl
import com.taras.pet.sharehubbroadcastcenter.data.repository.SystemEventsRepositoryImpl
import com.taras.pet.sharehubbroadcastcenter.domain.repository.ShareRepository
import com.taras.pet.sharehubbroadcastcenter.domain.repository.SystemEventsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SystemEventsModule {

    @Binds
    @Singleton
    abstract fun bindSystemEventsRepository(
        impl: SystemEventsRepositoryImpl
    ): SystemEventsRepository

}