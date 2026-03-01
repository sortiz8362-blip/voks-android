package com.voks.social.di

import com.voks.social.data.repository.AppwriteAuthRepository
import com.voks.social.data.repository.AppwriteDatabaseRepository
import com.voks.social.domain.repository.AuthRepository
import com.voks.social.domain.repository.DatabaseRepository
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

    // NUEVO: Vinculamos el repositorio de base de datos
    @Binds
    @Singleton
    abstract fun bindDatabaseRepository(
        appwriteDatabaseRepository: AppwriteDatabaseRepository
    ): DatabaseRepository
}