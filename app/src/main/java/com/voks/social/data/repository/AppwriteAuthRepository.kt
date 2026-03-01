package com.voks.social.data.repository

import com.voks.social.core.utils.Resource
import com.voks.social.domain.repository.AuthRepository
import io.appwrite.ID
import io.appwrite.models.Session
import io.appwrite.models.User
import io.appwrite.services.Account
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AppwriteAuthRepository @Inject constructor(
    private val account: Account
) : AuthRepository {

    override fun login(email: String, password: String): Flow<Resource<Session>> = flow {
        emit(Resource.Loading)
        try {
            val session = account.createEmailPasswordSession(email, password)
            emit(Resource.Success(session))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al iniciar sesión"))
        }
    }

    override fun register(name: String, email: String, password: String): Flow<Resource<User<Map<String, Any>>>> = flow {
        emit(Resource.Loading)
        try {
            val user = account.create(
                userId = ID.unique(),
                email = email,
                password = password,
                name = name
            )
            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al registrar usuario"))
        }
    }

    override fun logout(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            account.deleteSession("current")
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al cerrar sesión"))
        }
    }

    override fun checkAuthStatus(): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading)
        try {
            // Intenta obtener la cuenta actual. Si no hay sesión válida, lanza excepción.
            account.get()
            emit(Resource.Success(true))
        } catch (e: Exception) {
            emit(Resource.Success(false))
        }
    }

    // Novedades Fase 4: Seguridad y Verificación

    override fun getUser(): Flow<Resource<User<Map<String, Any>>>> = flow {
        emit(Resource.Loading)
        try {
            // Obtiene todos los detalles del usuario actual, incluyendo si su email está verificado
            val user = account.get()
            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al obtener datos del usuario"))
        }
    }

    override fun sendVerificationEmail(redirectUrl: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            // Appwrite envía automáticamente el correo de verificación configurado en el proyecto
            account.createVerification(url = redirectUrl)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al enviar el correo de verificación"))
        }
    }
}