package com.taras.pet.sharehubbroadcastcenter.data.di

import com.taras.pet.sharehubbroadcastcenter.data.repository.BroadcastRepositoryImpl
import com.taras.pet.sharehubbroadcastcenter.domain.repository.BroadcastRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BroadcastModule {

    @Binds
    @Singleton
    abstract fun bindBroadcastRepository(
        impl: BroadcastRepositoryImpl
    ): BroadcastRepository

}