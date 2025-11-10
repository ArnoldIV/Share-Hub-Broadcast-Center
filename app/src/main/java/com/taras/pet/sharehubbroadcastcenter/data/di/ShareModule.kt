package com.taras.pet.sharehubbroadcastcenter.data.di

import com.taras.pet.sharehubbroadcastcenter.data.repository.ShareRepositoryImpl
import com.taras.pet.sharehubbroadcastcenter.domain.repository.ShareRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ShareModule {

    @Binds
    @Singleton
    abstract fun bindShareRepository(
    impl: ShareRepositoryImpl
    ): ShareRepository

}