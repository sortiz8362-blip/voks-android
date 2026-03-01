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

    // CRUD Posts
    fun createPost(post: Post): Flow<Resource<Unit>>
    fun getPosts(): Flow<Resource<List<Post>>>

    // REGLA INQUEBRANTABLE: Mensajería EXCLUSIVA 1 a 1
    fun sendMessage(message: Message): Flow<Resource<Unit>>
    fun getMessages(user1Id: String, user2Id: String): Flow<Resource<List<Message>>>
}