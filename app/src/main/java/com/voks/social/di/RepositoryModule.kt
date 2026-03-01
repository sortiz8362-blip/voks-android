package com.voks.social.di

import com.voks.social.data.repository.AppwriteAuthRepository
import com.voks.social.domain.repository.AuthRepository
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
}