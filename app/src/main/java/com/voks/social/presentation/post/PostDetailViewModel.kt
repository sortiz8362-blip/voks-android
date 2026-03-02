package com.voks.social.presentation.post

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voks.social.core.utils.Resource
import com.voks.social.domain.model.Comment
import com.voks.social.domain.model.Post
import com.voks.social.domain.model.User
import com.voks.social.domain.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.services.Account
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

// Clase auxiliar para vincular el comentario con los datos de su autor
data class CommentWithUser(
    val comment: Comment,
    val user: User?
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val account: Account, // Usamos Account directamente de Appwrite para máxima seguridad
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Recuperamos el postId de los argumentos de navegación
    private val postId: String = checkNotNull(savedStateHandle["postId"])

    private val _post = MutableStateFlow<Post?>(null)
    val post: StateFlow<Post?> = _post.asStateFlow()

    private val _postAuthor = MutableStateFlow<User?>(null)
    val postAuthor: StateFlow<User?> = _postAuthor.asStateFlow()

    private val _comments = MutableStateFlow<List<CommentWithUser>>(emptyList())
    val comments: StateFlow<List<CommentWithUser>> = _comments.asStateFlow()

    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    var currentUserId: String = ""
        private set

    init {
        viewModelScope.launch {
            // Obtenemos el ID del usuario directamente de la sesión activa de Appwrite
            try {
                val sessionAccount = account.get()
                currentUserId = sessionAccount.id
            } catch (e: Exception) {
                // Manejo silencioso en caso de no estar autenticado
            }

            loadPostDetails()
        }
    }

    fun loadPostDetails() {
        viewModelScope.launch {
            _isLoading.value = true

            // 1. Obtener el Post
            val postResource = databaseRepository.getPost(postId).first()
            if (postResource is Resource.Success) {
                _post.value = postResource.data

                // 2. Obtener el Autor del Post
                val authorResource = databaseRepository.getUser(postResource.data.userId).first()
                if (authorResource is Resource.Success) {
                    _postAuthor.value = authorResource.data
                }
            }

            // 3. Obtener Comentarios
            loadComments()
            _isLoading.value = false
        }
    }

    private suspend fun loadComments() {
        databaseRepository.getCommentsForPost(postId).collect { resource ->
            if (resource is Resource.Success) {
                val commentsList = resource.data
                // Emparejar cada comentario con su usuario correspondiente
                val commentsWithUsers = commentsList.map { comment ->
                    val userRes = databaseRepository.getUser(comment.userId).first()
                    val user = if (userRes is Resource.Success) userRes.data else null
                    CommentWithUser(comment, user)
                }
                _comments.value = commentsWithUsers
            }
        }
    }

    fun onCommentTextChanged(text: String) {
        _commentText.value = text
    }

    fun sendComment() {
        val content = _commentText.value.trim()
        if (content.isEmpty() || currentUserId.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            val newComment = Comment(
                postId = postId,
                userId = currentUserId,
                content = content
            )

            databaseRepository.addComment(newComment).collect { resource ->
                if (resource is Resource.Success) {
                    _commentText.value = "" // Limpiar el campo
                    loadComments() // Recargar hilos
                }
                _isLoading.value = false
            }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            if (currentUserId.isNotEmpty()) {
                databaseRepository.toggleLike(postId, currentUserId).collect {
                    if (it is Resource.Success) loadPostDetails()
                }
            }
        }
    }
}