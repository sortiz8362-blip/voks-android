package com.voks.social.presentation.post

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voks.social.core.utils.Constants
import com.voks.social.core.utils.Resource
import com.voks.social.domain.model.Post
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
    private val authRepository: AuthRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _createPostState = MutableStateFlow<Resource<Unit>?>(null)
    val createPostState: StateFlow<Resource<Unit>?> = _createPostState.asStateFlow()

    private val _selectedMediaUri = MutableStateFlow<Uri?>(null)
    val selectedMediaUri: StateFlow<Uri?> = _selectedMediaUri.asStateFlow()

    fun setMediaUri(uri: Uri?) {
        _selectedMediaUri.value = uri
    }

    fun createPost(content: String) {
        if (content.isBlank() && _selectedMediaUri.value == null) return

        viewModelScope.launch {
            _createPostState.value = Resource.Loading

            try {
                authRepository.getUser().collect { userResource ->
                    when (userResource) {
                        is Resource.Success -> {
                            val userId = userResource.data.id
                            var finalMediaUrl = ""

                            val currentUri = _selectedMediaUri.value
                            if (currentUri != null) {
                                // FASE 10: Le indicamos que suba al Bucket de Posts
                                val uploadResult = storageRepository.uploadMedia(currentUri, Constants.POST_IMAGES_BUCKET_ID)
                                if (uploadResult is Resource.Success) {
                                    finalMediaUrl = uploadResult.data
                                } else if (uploadResult is Resource.Error) {
                                    _createPostState.value = Resource.Error(uploadResult.message ?: "Error al subir multimedia")
                                    return@collect
                                }
                            }

                            val newPost = Post(
                                userId = userId,
                                content = content.trim(),
                                imageUrl = finalMediaUrl,
                                likes = emptyList()
                            )

                            databaseRepository.createPost(newPost).collect { result ->
                                _createPostState.value = result
                                if (result is Resource.Success) {
                                    _selectedMediaUri.value = null
                                }
                            }
                        }
                        is Resource.Error -> {
                            _createPostState.value = Resource.Error(userResource.message)
                        }
                        is Resource.Loading -> {}
                    }
                }
            } catch (e: Exception) {
                _createPostState.value = Resource.Error(e.message ?: "Ocurrió un error inesperado al publicar.")
            }
        }
    }

    fun resetCreatePostState() {
        _createPostState.value = null
        _selectedMediaUri.value = null
    }
}