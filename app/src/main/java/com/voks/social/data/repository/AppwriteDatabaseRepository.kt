package com.voks.social.data.repository

import com.voks.social.core.utils.Constants
import com.voks.social.core.utils.Resource
import com.voks.social.domain.model.Comment
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
                "bannerUrl" to user.bannerUrl,
                "bio" to user.bio,
                "followers" to user.followers,
                "following" to user.following,
                "bookmarks" to user.bookmarks
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
                bannerUrl = data["bannerUrl"]?.toString() ?: "",
                bio = data["bio"]?.toString() ?: "",
                followers = (data["followers"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                following = (data["following"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                bookmarks = (data["bookmarks"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                createdAt = document.createdAt
            )
            emit(Resource.Success(user))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al obtener el perfil del usuario"))
        }
    }

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

    override fun followUser(currentUserId: String, targetUserId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val currentUserDoc = databases.getDocument(Constants.DATABASE_ID, Constants.USERS_COLLECTION_ID, currentUserId)
            val currentFollowing = (currentUserDoc.data["following"] as? List<*>)?.map { it.toString() }?.toMutableList() ?: mutableListOf()
            if (!currentFollowing.contains(targetUserId)) {
                currentFollowing.add(targetUserId)
                databases.updateDocument(
                    databaseId = Constants.DATABASE_ID,
                    collectionId = Constants.USERS_COLLECTION_ID,
                    documentId = currentUserId,
                    data = mapOf("following" to currentFollowing)
                )
            }

            val targetUserDoc = databases.getDocument(Constants.DATABASE_ID, Constants.USERS_COLLECTION_ID, targetUserId)
            val targetFollowers = (targetUserDoc.data["followers"] as? List<*>)?.map { it.toString() }?.toMutableList() ?: mutableListOf()
            if (!targetFollowers.contains(currentUserId)) {
                targetFollowers.add(currentUserId)
                databases.updateDocument(
                    databaseId = Constants.DATABASE_ID,
                    collectionId = Constants.USERS_COLLECTION_ID,
                    documentId = targetUserId,
                    data = mapOf("followers" to targetFollowers)
                )
            }
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al seguir al usuario"))
        }
    }

    override fun unfollowUser(currentUserId: String, targetUserId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val currentUserDoc = databases.getDocument(Constants.DATABASE_ID, Constants.USERS_COLLECTION_ID, currentUserId)
            val currentFollowing = (currentUserDoc.data["following"] as? List<*>)?.map { it.toString() }?.toMutableList() ?: mutableListOf()
            if (currentFollowing.contains(targetUserId)) {
                currentFollowing.remove(targetUserId)
                databases.updateDocument(
                    databaseId = Constants.DATABASE_ID,
                    collectionId = Constants.USERS_COLLECTION_ID,
                    documentId = currentUserId,
                    data = mapOf("following" to currentFollowing)
                )
            }

            val targetUserDoc = databases.getDocument(Constants.DATABASE_ID, Constants.USERS_COLLECTION_ID, targetUserId)
            val targetFollowers = (targetUserDoc.data["followers"] as? List<*>)?.map { it.toString() }?.toMutableList() ?: mutableListOf()
            if (targetFollowers.contains(currentUserId)) {
                targetFollowers.remove(currentUserId)
                databases.updateDocument(
                    databaseId = Constants.DATABASE_ID,
                    collectionId = Constants.USERS_COLLECTION_ID,
                    documentId = targetUserId,
                    data = mapOf("followers" to targetFollowers)
                )
            }
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al dejar de seguir al usuario"))
        }
    }

    override fun createPost(post: Post): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val mapData = mapOf(
                "userId" to post.userId,
                "content" to post.content,
                "imageUrl" to post.imageUrl,
                "likes" to post.likes,
                "reposts" to post.reposts,
                "originalPostId" to post.originalPostId
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
                    reposts = (data["reposts"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                    originalPostId = data["originalPostId"]?.toString() ?: "",
                    createdAt = document.createdAt
                )
            }
            emit(Resource.Success(posts))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al obtener el feed"))
        }
    }

    override fun getPost(postId: String): Flow<Resource<Post>> = flow {
        emit(Resource.Loading)
        try {
            val document = databases.getDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.POSTS_COLLECTION_ID,
                documentId = postId
            )
            val data = document.data
            val post = Post(
                id = document.id,
                userId = data["userId"]?.toString() ?: "",
                content = data["content"]?.toString() ?: "",
                imageUrl = data["imageUrl"]?.toString() ?: "",
                likes = (data["likes"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                reposts = (data["reposts"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                originalPostId = data["originalPostId"]?.toString() ?: "",
                createdAt = document.createdAt
            )
            emit(Resource.Success(post))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al cargar el detalle del post"))
        }
    }

    // --- FASE 13: Likes y Bookmarks ---
    override fun toggleLike(postId: String, userId: String): Flow<Resource<Unit>> = flow {
        try {
            val doc = databases.getDocument(Constants.DATABASE_ID, Constants.POSTS_COLLECTION_ID, postId)
            val likes = (doc.data["likes"] as? List<*>)?.map { it.toString() }?.toMutableList() ?: mutableListOf()

            if (likes.contains(userId)) likes.remove(userId) else likes.add(userId)

            databases.updateDocument(
                Constants.DATABASE_ID, Constants.POSTS_COLLECTION_ID, postId,
                data = mapOf("likes" to likes)
            )
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al procesar el Me gusta"))
        }
    }

    override fun toggleBookmark(userId: String, postId: String): Flow<Resource<Unit>> = flow {
        try {
            val doc = databases.getDocument(Constants.DATABASE_ID, Constants.USERS_COLLECTION_ID, userId)
            val bookmarks = (doc.data["bookmarks"] as? List<*>)?.map { it.toString() }?.toMutableList() ?: mutableListOf()

            if (bookmarks.contains(postId)) bookmarks.remove(postId) else bookmarks.add(postId)

            databases.updateDocument(
                Constants.DATABASE_ID, Constants.USERS_COLLECTION_ID, userId,
                data = mapOf("bookmarks" to bookmarks)
            )
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al procesar el guardado"))
        }
    }

    // --- FASE 15: Reposts ---
    override fun toggleRepost(postId: String, userId: String): Flow<Resource<Unit>> = flow {
        try {
            // 1. Obtenemos el post original para actualizar su contador
            val doc = databases.getDocument(Constants.DATABASE_ID, Constants.POSTS_COLLECTION_ID, postId)
            val reposts = (doc.data["reposts"] as? List<*>)?.map { it.toString() }?.toMutableList() ?: mutableListOf()

            val isReposting = !reposts.contains(userId)

            if (isReposting) {
                reposts.add(userId)
                // 2. Creamos el nuevo documento "Repost" para propagarlo en el Feed de los seguidores
                val mapData = mapOf(
                    "userId" to userId,
                    "content" to "",
                    "imageUrl" to "",
                    "likes" to emptyList<String>(),
                    "reposts" to emptyList<String>(),
                    "originalPostId" to postId
                )
                databases.createDocument(
                    databaseId = Constants.DATABASE_ID,
                    collectionId = Constants.POSTS_COLLECTION_ID,
                    documentId = ID.unique(),
                    data = mapData
                )
            } else {
                reposts.remove(userId)
                // 3. Deshacer el Repost: Lo buscamos localmente para evitar errores de índices en Appwrite
                val result = databases.listDocuments(
                    Constants.DATABASE_ID, Constants.POSTS_COLLECTION_ID,
                    listOf(Query.orderDesc("\$createdAt"))
                )
                val repostDoc = result.documents.find {
                    it.data["userId"]?.toString() == userId &&
                            it.data["originalPostId"]?.toString() == postId &&
                            it.data["content"]?.toString().isNullOrEmpty()
                }
                repostDoc?.let {
                    databases.deleteDocument(Constants.DATABASE_ID, Constants.POSTS_COLLECTION_ID, it.id)
                }
            }

            // 4. Actualizamos la matriz de reposts en el documento original
            databases.updateDocument(
                Constants.DATABASE_ID, Constants.POSTS_COLLECTION_ID, postId,
                data = mapOf("reposts" to reposts)
            )
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al procesar el Repost"))
        }
    }

    // --- FASE 14: Comentarios ---
    override fun addComment(comment: Comment): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        try {
            val mapData = mapOf(
                "postId" to comment.postId,
                "userId" to comment.userId,
                "content" to comment.content
            )
            databases.createDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COMMENTS_COLLECTION_ID,
                documentId = ID.unique(),
                data = mapData
            )
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al añadir comentario"))
        }
    }

    override fun getCommentsForPost(postId: String): Flow<Resource<List<Comment>>> = flow {
        emit(Resource.Loading)
        try {
            val result = databases.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.COMMENTS_COLLECTION_ID,
                queries = listOf(
                    Query.equal("postId", postId),
                    Query.orderAsc("\$createdAt")
                )
            )

            val comments = result.documents.map { document ->
                val data = document.data
                Comment(
                    id = document.id,
                    postId = data["postId"]?.toString() ?: "",
                    userId = data["userId"]?.toString() ?: "",
                    content = data["content"]?.toString() ?: "",
                    createdAt = document.createdAt
                )
            }
            emit(Resource.Success(comments))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error al obtener comentarios"))
        }
    }

    // --- Mensajería ---
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