package com.voks.social.data.repository

import com.voks.social.core.utils.Constants
import com.voks.social.core.utils.Resource
import com.voks.social.domain.model.Message
import com.voks.social.domain.model.Post
import com.voks.social.domain.model.User
import com.voks.social.domain.repository.DatabaseRepository
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.services.Databases
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AppwriteDatabaseRepository @Inject constructor(
    private val databases: Databases
) : DatabaseRepository {

    override fun saveUser(user: User): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val mapData = mapOf(
                "username" to user.username,
                "email" to user.email,
                "profilePictureUrl" to user.profilePictureUrl,
                "bannerUrl" to user.bannerUrl, // AÑADIDO: Guardar banner
                "bio" to user.bio,
                "followers" to user.followers,
                "following" to user.following
            )
            databases.createDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.USERS_COLLECTION_ID,
                documentId = user.id.ifEmpty { ID.unique() },
                data = mapData
            )
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al guardar el usuario en la base de datos"))
        }
    }

    override fun getUser(userId: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        try {
            val document = databases.getDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.USERS_COLLECTION_ID,
                documentId = userId
            )
            val data = document.data
            val user = User(
                id = document.id,
                username = data["username"]?.toString() ?: "",
                email = data["email"]?.toString() ?: "",
                profilePictureUrl = data["profilePictureUrl"]?.toString() ?: "",
                bannerUrl = data["bannerUrl"]?.toString() ?: "", // AÑADIDO: Recuperar banner
                bio = data["bio"]?.toString() ?: "",
                followers = (data["followers"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                following = (data["following"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                createdAt = document.createdAt
            )
            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al obtener el perfil del usuario"))
        }
    }

    // NUEVO FASE 10: Implementación para actualizar solo los campos modificados
    override fun updateUser(userId: String, data: Map<String, Any>): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            databases.updateDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.USERS_COLLECTION_ID,
                documentId = userId,
                data = data
            )
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al actualizar el perfil"))
        }
    }

    override fun createPost(post: Post): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val mapData = mapOf(
                "userId" to post.userId,
                "content" to post.content,
                "imageUrl" to post.imageUrl,
                "likes" to post.likes
            )
            databases.createDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.POSTS_COLLECTION_ID,
                documentId = ID.unique(),
                data = mapData
            )
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al crear la publicación"))
        }
    }

    override fun getPosts(): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading)
        try {
            val result = databases.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.POSTS_COLLECTION_ID,
                queries = listOf(Query.orderDesc("\$createdAt"))
            )

            val posts = result.documents.map { document ->
                val data = document.data
                Post(
                    id = document.id,
                    userId = data["userId"]?.toString() ?: "",
                    content = data["content"]?.toString() ?: "",
                    imageUrl = data["imageUrl"]?.toString() ?: "",
                    likes = (data["likes"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                    createdAt = document.createdAt
                )
            }
            emit(Resource.Success(posts))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al obtener el feed"))
        }
    }

    override fun sendMessage(message: Message): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val mapData = mapOf(
                "senderId" to message.senderId,
                "receiverId" to message.receiverId,
                "content" to message.content,
                "isRead" to message.isRead
            )
            databases.createDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.MESSAGES_COLLECTION_ID,
                documentId = ID.unique(),
                data = mapData
            )
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al enviar el mensaje"))
        }
    }

    override fun getMessages(user1Id: String, user2Id: String): Flow<Resource<List<Message>>> = flow {
        emit(Resource.Loading)
        try {
            val result = databases.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.MESSAGES_COLLECTION_ID,
                queries = listOf(
                    Query.equal("senderId", listOf(user1Id, user2Id)),
                    Query.equal("receiverId", listOf(user1Id, user2Id)),
                    Query.orderAsc("\$createdAt")
                )
            )

            val messages = result.documents.map { document ->
                val data = document.data
                Message(
                    id = document.id,
                    senderId = data["senderId"]?.toString() ?: "",
                    receiverId = data["receiverId"]?.toString() ?: "",
                    content = data["content"]?.toString() ?: "",
                    isRead = data["isRead"] as? Boolean ?: false,
                    createdAt = document.createdAt
                )
            }
            emit(Resource.Success(messages))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al cargar el historial del chat"))
        }
    }
}