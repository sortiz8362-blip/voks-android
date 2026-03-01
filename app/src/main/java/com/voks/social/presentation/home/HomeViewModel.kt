package com.voks.social.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voks.social.core.utils.Resource
import com.voks.social.domain.model.Post
import com.voks.social.domain.model.User
import com.voks.social.domain.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Modelo de datos exclusivo para la UI que une el Post con los datos de su Autor
data class PostUiItem(
    val post: Post,
    val user: User? // Será null temporalmente mientras descarga los datos del usuario
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository
) : ViewModel() {

    private val _feedState = MutableStateFlow<Resource<List<PostUiItem>>>(Resource.Loading)
    val feedState: StateFlow<Resource<List<PostUiItem>>> = _feedState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Caché en memoria para no descargar el mismo perfil varias veces si el usuario publicó mucho
    private val userCache = mutableMapOf<String, User>()

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _feedState.value = Resource.Loading
            fetchPostsFromDatabase()
        }
    }

    fun refreshFeed() {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchPostsFromDatabase()
            _isRefreshing.value = false
        }
    }

    private suspend fun fetchPostsFromDatabase() {
        databaseRepository.getPosts().collect { result ->
            when (result) {
                is Resource.Success -> {
                    val posts = result.data
                    // Inicializamos la lista de UI sin los usuarios
                    val uiItems = posts.map { PostUiItem(it, userCache[it.userId]) }
                    _feedState.value = Resource.Success(uiItems)

                    // Buscamos los usuarios que faltan en la caché
                    loadUsersForPosts(posts)
                }
                is Resource.Error -> {
                    _feedState.value = Resource.Error(result.message)
                }
                is Resource.Loading -> {
                    if (_feedState.value !is Resource.Success) {
                        _feedState.value = Resource.Loading
                    }
                }
            }
        }
    }

    private fun loadUsersForPosts(posts: List<Post>) {
        viewModelScope.launch {
            // Obtenemos solo los IDs únicos para no hacer llamadas repetidas
            val uniqueUserIds = posts.map { it.userId }.distinct()

            uniqueUserIds.forEach { userId ->
                if (!userCache.containsKey(userId)) {
                    databaseRepository.getUser(userId).collect { userResult ->
                        if (userResult is Resource.Success) {
                            userCache[userId] = userResult.data
                            updateFeedWithCachedUsers() // Refrescamos la UI cada vez que llega un usuario nuevo
                        }
                    }
                }
            }
        }
    }

    private fun updateFeedWithCachedUsers() {
        val currentState = _feedState.value
        if (currentState is Resource.Success) {
            val updatedItems = currentState.data.map { item ->
                item.copy(user = userCache[item.post.userId])
            }
            _feedState.value = Resource.Success(updatedItems)
        }
    }
}