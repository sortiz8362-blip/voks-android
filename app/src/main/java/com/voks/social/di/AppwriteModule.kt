package com.voks.social.di

import android.content.Context
import com.voks.social.core.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppwriteModule {

    @Provides
    @Singleton
    fun provideAppwriteClient(@ApplicationContext context: Context): Client {
        return Client(context)
            .setEndpoint(Constants.APPWRITE_ENDPOINT)
            .setProject(Constants.APPWRITE_PROJECT_ID)
    }

    @Provides
    @Singleton
    fun provideAppwriteAccount(client: Client): Account {
        return Account(client)
    }

    @Provides
    @Singleton
    fun provideAppwriteDatabases(client: Client): Databases {
        return Databases(client)
    }

    // AQUI ESTÁ LA SOLUCIÓN: Hilt ya sabe cómo inyectar Storage
    @Provides
    @Singleton
    fun provideAppwriteStorage(client: Client): Storage {
        return Storage(client)
    }
}