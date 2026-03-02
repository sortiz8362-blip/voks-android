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

// FASE 13: Extendemos el modelo UI para saber de forma instantánea si nos gusta o lo guardamos
data class PostUiItem(
    val post: Post,
    val user: User?,
    val isLikedByMe: Boolean = false,
    val isBookmarkedByMe: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _feedState = MutableStateFlow<Resource<List<PostUiItem>>>(Resource.Loading)
    val feedState: StateFlow<Resource<List<PostUiItem>>> = _feedState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val userCache = mutableMapOf<String, User>()

    private var allPostsCache = listOf<PostUiItem>()
    private var myUserId = ""
    private var myFollowing = listOf<String>()
    private var myBookmarks = listOf<String>() // FASE 13: Guardamos los IDs de los posts cacheados

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

    fun setTab(tabIndex: Int) {
        if (_selectedTab.value != tabIndex) {
            _selectedTab.value = tabIndex
            updateFeedUI()
        }
    }

    private suspend fun fetchCurrentUserAndPosts() {
        authRepository.getUser().collect { authResult ->
            when (authResult) {
                is Resource.Success -> {
                    myUserId = authResult.data.id

                    databaseRepository.getUser(myUserId).collect { dbUserResult ->
                        if (dbUserResult is Resource.Success) {
                            myFollowing = dbUserResult.data.following
                            myBookmarks = dbUserResult.data.bookmarks // FASE 13
                            userCache[myUserId] = dbUserResult.data
                        }

                        if (dbUserResult !is Resource.Loading) {
                            fetchPostsFromDatabase()
                        }
                    }
                }
                is Resource.Error -> {
                    _feedState.value = Resource.Error(authResult.message ?: "Error de autenticación")
                }
                is Resource.Loading -> { }
            }
        }
    }

    private suspend fun fetchPostsFromDatabase() {
        databaseRepository.getPosts().collect { result ->
            when (result) {
                is Resource.Success -> {
                    val posts = result.data

                    // FASE 13: Mapeamos reconociendo mis likes y mis bookmarks
                    allPostsCache = posts.map { post ->
                        PostUiItem(
                            post = post,
                            user = userCache[post.userId],
                            isLikedByMe = post.likes.contains(myUserId),
                            isBookmarkedByMe = myBookmarks.contains(post.id)
                        )
                    }

                    updateFeedUI()
                    loadUsersForPosts(posts)
                }
                is Resource.Error -> _feedState.value = Resource.Error(result.message)
                is Resource.Loading -> {
                    if (_feedState.value !is Resource.Success) _feedState.value = Resource.Loading
                }
            }
        }
    }

    private fun loadUsersForPosts(posts: List<Post>) {
        viewModelScope.launch {
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
        allPostsCache = allPostsCache.map { item ->
            item.copy(user = userCache[item.post.userId])
        }
        updateFeedUI()
    }

    private fun updateFeedUI() {
        val currentTab = _selectedTab.value

        val filteredList = if (currentTab == 0) {
            allPostsCache
        } else {
            allPostsCache.filter { it.post.userId == myUserId || myFollowing.contains(it.post.userId) }
        }

        _feedState.value = Resource.Success(filteredList)
    }

    // --- FASE 13: Lógica de Interacciones (Actualización Optimista) ---

    fun toggleLike(postId: String) {
        if (myUserId.isEmpty()) return

        // 1. Actualización Optimista (Reflejo instantáneo en UI)
        allPostsCache = allPostsCache.map { item ->
            if (item.post.id == postId) {
                val newLikes = item.post.likes.toMutableList()
                val isCurrentlyLiked = newLikes.contains(myUserId)

                if (isCurrentlyLiked) newLikes.remove(myUserId) else newLikes.add(myUserId)

                item.copy(
                    post = item.post.copy(likes = newLikes),
                    isLikedByMe = !isCurrentlyLiked
                )
            } else item
        }
        updateFeedUI()

        // 2. Ejecutar petición en backend silenciosamente
        viewModelScope.launch {
            databaseRepository.toggleLike(postId, myUserId).collect { }
        }
    }

    fun toggleBookmark(postId: String) {
        if (myUserId.isEmpty()) return

        // 1. Actualización Optimista
        allPostsCache = allPostsCache.map { item ->
            if (item.post.id == postId) {
                item.copy(isBookmarkedByMe = !item.isBookmarkedByMe)
            } else item
        }
        updateFeedUI()

        // 2. Petición a base de datos
        viewModelScope.launch {
            databaseRepository.toggleBookmark(myUserId, postId).collect { }
        }
    }
}