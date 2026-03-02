package com.voks.social.domain.repository

import com.voks.social.core.utils.Resource
import com.voks.social.domain.model.Message
import com.voks.social.domain.model.Post
import com.voks.social.domain.model.User
import kotlinx.coroutines.flow.Flow

interface DatabaseRepository {
    // CRUD Usuarios
    fun saveUser(user: User): Flow<Resource<Unit>>
    fun getUser(userId: String): Flow<Resource<User>>

    // Actualizar datos de un usuario (Bio, Foto, Banner)
    fun updateUser(userId: String, data: Map<String, Any>): Flow<Resource<Unit>>

    // FASE 11: Conexiones (Seguir / Dejar de seguir)
    fun followUser(currentUserId: String, targetUserId: String): Flow<Resource<Unit>>
    fun unfollowUser(currentUserId: String, targetUserId: String): Flow<Resource<Unit>>

    // CRUD Posts
    fun createPost(post: Post): Flow<Resource<Unit>>
    fun getPosts(): Flow<Resource<List<Post>>>

    // Mensajería
    fun sendMessage(message: Message): Flow<Resource<Unit>>
    fun getMessages(user1Id: String, user2Id: String): Flow<Resource<List<Message>>>
}