package com.voks.social.presentation.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voks.social.core.utils.Resource
import com.voks.social.domain.model.Post
import com.voks.social.domain.repository.AuthRepository
import com.voks.social.domain.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _createPostState = MutableStateFlow<Resource<Unit>?>(null)
    val createPostState: StateFlow<Resource<Unit>?> = _createPostState.asStateFlow()

    fun createPost(content: String) {
        // Validación básica: no permitir posts vacíos
        if (content.isBlank()) return

        viewModelScope.launch {
            _createPostState.value = Resource.Loading

            try {
                // CORRECCIÓN: Eliminamos el uso de ".first()"
                // Usar .first() cancelaba el Flow prematuramente, lanzando un CancellationException
                // que nuestro repositorio atrapaba por error (violando la transparencia del Flow).
                // Ahora recolectamos el Flow de forma segura con .collect {}
                authRepository.getUser().collect { userResource ->
                    when (userResource) {
                        is Resource.Success -> {
                            val userId = userResource.data.id

                            // Construimos el modelo Post
                            val newPost = Post(
                                userId = userId,
                                content = content.trim(),
                                imageUrl = "",
                                likes = emptyList()
                            )

                            // Enviamos a la base de datos de Appwrite
                            databaseRepository.createPost(newPost).collect { result ->
                                _createPostState.value = result
                            }
                        }
                        is Resource.Error -> {
                            _createPostState.value = Resource.Error(userResource.message)
                        }
                        is Resource.Loading -> {
                            // Mantenemos el estado de carga
                        }
                    }
                }
            } catch (e: Exception) {
                _createPostState.value = Resource.Error(e.message ?: "Ocurrió un error inesperado al publicar.")
            }
        }
    }

    // Función útil para limpiar el estado una vez que salimos de la pantalla de creación
    fun resetCreatePostState() {
        _createPostState.value = null
    }
}