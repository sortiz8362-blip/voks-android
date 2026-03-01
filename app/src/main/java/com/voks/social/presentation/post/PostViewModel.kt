package com.voks.social.presentation.post

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val storageRepository: StorageRepository // NUEVO FASE 8
) : ViewModel() {

    private val _createPostState = MutableStateFlow<Resource<Unit>?>(null)
    val createPostState: StateFlow<Resource<Unit>?> = _createPostState.asStateFlow()

    // NUEVO FASE 8: Estado para almacenar temporalmente la Uri seleccionada
    private val _selectedMediaUri = MutableStateFlow<Uri?>(null)
    val selectedMediaUri: StateFlow<Uri?> = _selectedMediaUri.asStateFlow()

    fun setMediaUri(uri: Uri?) {
        _selectedMediaUri.value = uri
    }

    fun createPost(content: String) {
        // Validación: no permitir publicar si no hay texto y tampoco hay foto/video
        if (content.isBlank() && _selectedMediaUri.value == null) return

        viewModelScope.launch {
            _createPostState.value = Resource.Loading

            try {
                authRepository.getUser().collect { userResource ->
                    when (userResource) {
                        is Resource.Success -> {
                            val userId = userResource.data.id
                            var finalMediaUrl = ""

                            // 1. Si hay archivo multimedia, lo subimos primero al Storage
                            val currentUri = _selectedMediaUri.value
                            if (currentUri != null) {
                                val uploadResult = storageRepository.uploadMedia(currentUri)
                                if (uploadResult is Resource.Success) {
                                    finalMediaUrl = uploadResult.data
                                } else if (uploadResult is Resource.Error) {
                                    _createPostState.value = Resource.Error(uploadResult.message ?: "Error al subir multimedia")
                                    return@collect
                                }
                            }

                            // 2. Construimos el post (usando tu campo imageUrl original)
                            val newPost = Post(
                                userId = userId,
                                content = content.trim(),
                                imageUrl = finalMediaUrl,
                                likes = emptyList()
                            )

                            // 3. Lo guardamos en Appwrite Database respetando el Flow
                            databaseRepository.createPost(newPost).collect { result ->
                                _createPostState.value = result
                                if (result is Resource.Success) {
                                    _selectedMediaUri.value = null // Limpiar tras éxito
                                }
                            }
                        }
                        is Resource.Error -> {
                            _createPostState.value = Resource.Error(userResource.message)
                        }
                        is Resource.Loading -> {
                            // Mantener estado de carga
                        }
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