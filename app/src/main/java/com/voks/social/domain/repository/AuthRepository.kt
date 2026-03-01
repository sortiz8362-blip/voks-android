package com.voks.social.domain.repository

import com.voks.social.core.utils.Resource
import io.appwrite.models.Session
import io.appwrite.models.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(email: String, password: String): Flow<Resource<Session>>
    fun register(name: String, email: String, password: String): Flow<Resource<User<Map<String, Any>>>>
    fun logout(): Flow<Resource<Unit>>
    fun checkAuthStatus(): Flow<Resource<Boolean>>
}