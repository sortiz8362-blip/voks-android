package com.voks.social.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voks.social.core.utils.Resource
import com.voks.social.domain.model.Post
import com.voks.social.domain.model.User
import com.voks.social.domain.repository.AuthRepository
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
    private val databaseRepository: DatabaseRepository,
    private val authRepository: AuthRepository // FASE 12: Inyectado para saber quién está logueado
) : ViewModel() {

    private val _feedState = MutableStateFlow<Resource<List<PostUiItem>>>(Resource.Loading)
    val feedState: StateFlow<Resource<List<PostUiItem>>> = _feedState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // FASE 12: Estado de la pestaña activa (0 = Para ti, 1 = Siguiendo)
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Caché en memoria para no descargar el mismo perfil varias veces
    private val userCache = mutableMapOf<String, User>()

    // FASE 12: Listas para el filtrado local del doble feed
    private var allPostsCache = listOf<PostUiItem>()
    private var myUserId = ""
    private var myFollowing = listOf<String>()

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _feedState.value = Resource.Loading
            fetchCurrentUserAndPosts()
        }
    }

    fun refreshFeed() {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchCurrentUserAndPosts()
            _isRefreshing.value = false
        }
    }

    // FASE 12: Función para cambiar de pestaña desde la UI
    fun setTab(tabIndex: Int) {
        if (_selectedTab.value != tabIndex) {
            _selectedTab.value = tabIndex
            updateFeedUI() // Refiltramos los posts al instante
        }
    }

    // FASE 12: Obtenemos el usuario actual primero para conocer su lista de "Siguiendo"
    private suspend fun fetchCurrentUserAndPosts() {
        authRepository.getUser().collect { authResult ->
            when (authResult) {
                is Resource.Success -> {
                    myUserId = authResult.data.id

                    // Con el ID, pedimos su perfil en Base de Datos
                    databaseRepository.getUser(myUserId).collect { dbUserResult ->
                        if (dbUserResult is Resource.Success) {
                            myFollowing = dbUserResult.data.following
                            userCache[myUserId] = dbUserResult.data // Lo cacheamos
                        }

                        // Una vez resuelto el perfil (sea éxito o error), cargamos los posts
                        if (dbUserResult !is Resource.Loading) {
                            fetchPostsFromDatabase()
                        }
                    }
                }
                is Resource.Error -> {
                    _feedState.value = Resource.Error(authResult.message ?: "Error de autenticación")
                }
                is Resource.Loading -> { /* Ignorar hasta el resultado final */ }
            }
        }
    }

    private suspend fun fetchPostsFromDatabase() {
        databaseRepository.getPosts().collect { result ->
            when (result) {
                is Resource.Success -> {
                    val posts = result.data

                    // FASE 12: Guardamos todos los posts en el caché maestro
                    allPostsCache = posts.map { PostUiItem(it, userCache[it.userId]) }

                    // Mostramos la lista dependiendo de la pestaña seleccionada
                    updateFeedUI()

                    // Buscamos los usuarios que faltan en la caché (carga diferida)
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
                            updateFeedWithCachedUsers()
                        }
                    }
                }
            }
        }
    }

    private fun updateFeedWithCachedUsers() {
        // Enriquecemos la lista maestra con los perfiles recién descargados
        allPostsCache = allPostsCache.map { item ->
            item.copy(user = userCache[item.post.userId])
        }
        // Refrescamos la UI respetando la pestaña actual
        updateFeedUI()
    }

    // FASE 12: Lógica central del algoritmo de feed
    private fun updateFeedUI() {
        val currentTab = _selectedTab.value

        val filteredList = if (currentTab == 0) {
            // "Para ti": Retorna todos los posts sin restricciones
            allPostsCache
        } else {
            // "Siguiendo": Retorna solo posts del usuario actual o de gente que sigue
            allPostsCache.filter { it.post.userId == myUserId || myFollowing.contains(it.post.userId) }
        }

        _feedState.value = Resource.Success(filteredList)
    }
}