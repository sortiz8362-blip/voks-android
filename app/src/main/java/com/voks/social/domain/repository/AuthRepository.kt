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

    // Novedades Fase 4: Seguridad y Verificación
    fun getUser(): Flow<Resource<User<Map<String, Any>>>>
    fun sendVerificationEmail(redirectUrl: String): Flow<Resource<Unit>>

    // Novedades Fase 3 (Complemento): Recuperar Contraseña
    fun sendPasswordRecoveryEmail(email: String, redirectUrl: String): Flow<Resource<Unit>>
    // Nota: La función confirmPasswordRecovery() (para crear la nueva contraseña) se suele manejar
    // en una página web externa que intercepta el link del correo, pero la dejaremos lista por si decides
    // manejar los Deep Links directamente en la app.
    fun confirmPasswordRecovery(userId: String, secret: String, newPassword: String): Flow<Resource<Unit>>
}