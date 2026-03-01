package com.voks.social.di

import com.voks.social.data.repository.AppwriteAuthRepository
import com.voks.social.data.repository.AppwriteDatabaseRepository
import com.voks.social.data.repository.AppwriteStorageRepository
import com.voks.social.domain.repository.AuthRepository
import com.voks.social.domain.repository.DatabaseRepository
import com.voks.social.domain.repository.StorageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        appwriteAuthRepository: AppwriteAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDatabaseRepository(
        appwriteDatabaseRepository: AppwriteDatabaseRepository
    ): DatabaseRepository

    // NUEVO FASE 8: Vinculamos el Storage Repository limpiamente
    @Binds
    @Singleton
    abstract fun bindStorageRepository(
        appwriteStorageRepository: AppwriteStorageRepository
    ): StorageRepository
}