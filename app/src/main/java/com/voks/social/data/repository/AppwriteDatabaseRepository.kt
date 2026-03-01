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
                "bio" to user.bio,
                "followers" to user.followers,
                "following" to user.following
            )
            // Usamos el ID de autenticación del usuario como su Document ID en la base de datos
            // Esto permite que las búsquedas sean instantáneas (O(1)).
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
            // Traemos los posts ordenados por fecha descendente (los más nuevos primero)
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

    // --- REGLA INQUEBRANTABLE: CHAT PRIVADO 1 A 1 ---
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
            // Buscamos cualquier mensaje donde sender y receiver coincidan con los 2 IDs.
            // Esto aísla por completo el chat para que sea privado.
            val result = databases.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.MESSAGES_COLLECTION_ID,
                queries = listOf(
                    Query.equal("senderId", listOf(user1Id, user2Id)),
                    Query.equal("receiverId", listOf(user1Id, user2Id)),
                    Query.orderAsc("\$createdAt") // Ordenamos desde el más viejo arriba al más nuevo abajo
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