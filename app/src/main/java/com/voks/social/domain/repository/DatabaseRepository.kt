package com.voks.social.domain.repository

import com.voks.social.core.utils.Resource
import com.voks.social.domain.model.Comment
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

    // Conexiones (Seguir / Dejar de seguir)
    fun followUser(currentUserId: String, targetUserId: String): Flow<Resource<Unit>>
    fun unfollowUser(currentUserId: String, targetUserId: String): Flow<Resource<Unit>>

    // CRUD Posts
    fun createPost(post: Post): Flow<Resource<Unit>>
    fun getPosts(): Flow<Resource<List<Post>>>
    fun getPost(postId: String): Flow<Resource<Post>> // NUEVO: Para obtener un solo post

    // FASE 13: Interacciones Ligeras
    fun toggleLike(postId: String, userId: String): Flow<Resource<Unit>>
    fun toggleBookmark(userId: String, postId: String): Flow<Resource<Unit>>

    // FASE 14: Interacciones Profundas (Comentarios)
    fun addComment(comment: Comment): Flow<Resource<Unit>>
    fun getCommentsForPost(postId: String): Flow<Resource<List<Comment>>>

    // Mensajería
    fun sendMessage(message: Message): Flow<Resource<Unit>>
    fun getMessages(user1Id: String, user2Id: String): Flow<Resource<List<Message>>>
}