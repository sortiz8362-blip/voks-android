package com.voks.social.presentation.post

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voks.social.core.utils.Constants
import com.voks.social.core.utils.Resource
import com.voks.social.domain.model.Post
import com.voks.social.domain.model.User
import com.voks.social.domain.repository.AuthRepository
import com.voks.social.domain.repository.DatabaseRepository
import com.voks.social.domain.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val storageRepository: StorageRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle // Permite recuperar argumentos de la navegación
) : ViewModel() {

    // FASE 15: Extraer el ID citado si existe
    val quoteId: String? = savedStateHandle.get<String>("quoteId")

    private val _createState = MutableStateFlow<Resource<Unit>?>(null)
    val createState: StateFlow<Resource<Unit>?> = _createState.asStateFlow()

    // FASE 15: Post Citado y su Autor para mostrarlos como Preview en el compositor
    private val _quotedPost = MutableStateFlow<Post?>(null)
    val quotedPost = _quotedPost.asStateFlow()

    private val _quotedPostUser = MutableStateFlow<User?>(null)
    val quotedPostUser = _quotedPostUser.asStateFlow()

    init {
        quoteId?.let { fetchQuotedPost(it) }
    }

    private fun fetchQuotedPost(id: String) {
        viewModelScope.launch {
            databaseRepository.getPost(id).collect { result ->
                if (result is Resource.Success) {
                    _quotedPost.value = result.data
                    fetchQuotedPostUser(result.data.userId)
                }
            }
        }
    }

    private fun fetchQuotedPostUser(userId: String) {
        viewModelScope.launch {
            databaseRepository.getUser(userId).collect { result ->
                if (result is Resource.Success) {
                    _quotedPostUser.value = result.data
                }
            }
        }
    }

    fun createPost(content: String, imageUri: Uri? = null) {
        viewModelScope.launch {
            _createState.value = Resource.Loading
            authRepository.getUser().collect { authResult ->
                when (authResult) {
                    is Resource.Success -> {
                        val userId = authResult.data.id

                        if (imageUri != null) {
                            val storageResult = storageRepository.uploadMedia(imageUri, Constants.POST_IMAGES_BUCKET_ID)
                            when (storageResult) {
                                is Resource.Success -> savePostToDatabase(userId, content, storageResult.data)
                                is Resource.Error -> _createState.value = Resource.Error(storageResult.message)
                                is Resource.Loading -> {}
                            }
                        } else {
                            savePostToDatabase(userId, content, "")
                        }
                    }
                    is Resource.Error -> {
                        _createState.value = Resource.Error("Error de autenticación")
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    private suspend fun savePostToDatabase(userId: String, content: String, imageUrl: String) {
        val post = Post(
            userId = userId,
            content = content,
            imageUrl = imageUrl,
            originalPostId = quoteId ?: "" // FASE 15: Vinculamos el post original
        )
        databaseRepository.createPost(post).collect { dbResult ->
            _createState.value = dbResult
        }
    }

    fun resetState() {
        _createState.value = null
    }
}