package com.voks.social.presentation.profile

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
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val databaseRepository: DatabaseRepository,
    private val storageRepository: StorageRepository,
    savedStateHandle: SavedStateHandle // FASE 11: Recuperamos argumentos de navegación
) : ViewModel() {

    // Extraemos el ID del usuario que queremos ver. Si es nulo, mostraremos nuestro propio perfil.
    private val targetUserId: String? = savedStateHandle.get<String>("userId")
    private var myUserId: String = "" // Guardamos nuestro ID interno para comparar

    private val _userProfile = MutableStateFlow<Resource<User>>(Resource.Loading)
    val userProfile: StateFlow<Resource<User>> = _userProfile.asStateFlow()

    private val _userPosts = MutableStateFlow<Resource<List<Post>>>(Resource.Loading)
    val userPosts: StateFlow<Resource<List<Post>>> = _userPosts.asStateFlow()

    private val _updateProfileState = MutableStateFlow<Resource<Unit>?>(null)
    val updateProfileState: StateFlow<Resource<Unit>?> = _updateProfileState.asStateFlow()

    // FASE 11: Estados para manejar si es mi perfil y si sigo a este usuario
    private val _isCurrentUser = MutableStateFlow(true)
    val isCurrentUser: StateFlow<Boolean> = _isCurrentUser.asStateFlow()

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    private val _followActionState = MutableStateFlow<Resource<Unit>?>(null)
    val followActionState: StateFlow<Resource<Unit>?> = _followActionState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            authRepository.getUser().collect { resource ->
                if (resource is Resource.Success) {
                    myUserId = resource.data.id

                    // Si recibimos un targetUserId usamos ese, de lo contrario usamos el nuestro
                    val profileToLoadId = targetUserId ?: myUserId
                    _isCurrentUser.value = (profileToLoadId == myUserId)

                    databaseRepository.getUser(profileToLoadId).collect { userResource ->
                        _userProfile.value = userResource
                        if (userResource is Resource.Success) {

                            // Validar si nuestro ID está en su lista de seguidores
                            val followersList = userResource.data.followers
                            _isFollowing.value = followersList.contains(myUserId)

                            loadUserPosts(profileToLoadId)
                        }
                    }
                } else if (resource is Resource.Error) {
                    _userProfile.value = Resource.Error(resource.message ?: "Error de autenticación")
                }
            }
        }
    }

    private fun loadUserPosts(userId: String) {
        viewModelScope.launch {
            databaseRepository.getPosts().collect { resource ->
                if (resource is Resource.Success) {
                    val filteredPosts = resource.data.filter { it.userId == userId }
                    _userPosts.value = Resource.Success(filteredPosts)
                } else if (resource is Resource.Error) {
                    _userPosts.value = Resource.Error(resource.message ?: "Error al cargar posts del usuario")
                }
            }
        }
    }

    // FASE 11: Ejecuta la acción de Seguir o Dejar de Seguir
    fun toggleFollow() {
        val targetId = targetUserId ?: return
        if (myUserId.isEmpty() || myUserId == targetId) return // Seguridad

        viewModelScope.launch {
            _followActionState.value = Resource.Loading
            val currentFollowingStatus = _isFollowing.value

            val flow = if (currentFollowingStatus) {
                databaseRepository.unfollowUser(myUserId, targetId)
            } else {
                databaseRepository.followUser(myUserId, targetId)
            }

            flow.collect { result ->
                _followActionState.value = result
                if (result is Resource.Success) {
                    _isFollowing.value = !currentFollowingStatus
                    loadUserProfile() // Recargamos para refrescar los contadores visuales
                }
            }
        }
    }

    fun updateProfile(bio: String, newAvatarUri: Uri?, newBannerUri: Uri?) {
        viewModelScope.launch {
            _updateProfileState.value = Resource.Loading
            try {
                val currentUser = (_userProfile.value as? Resource.Success)?.data ?: return@launch
                var avatarUrl = currentUser.profilePictureUrl
                var bannerUrl = currentUser.bannerUrl

                if (newAvatarUri != null) {
                    val avatarUpload = storageRepository.uploadMedia(newAvatarUri, Constants.PROFILE_IMAGES_BUCKET_ID)
                    if (avatarUpload is Resource.Success) {
                        avatarUrl = avatarUpload.data
                    } else {
                        _updateProfileState.value = Resource.Error("Error al subir foto de perfil")
                        return@launch
                    }
                }

                if (newBannerUri != null) {
                    val bannerUpload = storageRepository.uploadMedia(newBannerUri, Constants.PROFILE_IMAGES_BUCKET_ID)
                    if (bannerUpload is Resource.Success) {
                        bannerUrl = bannerUpload.data
                    } else {
                        _updateProfileState.value = Resource.Error("Error al subir banner")
                        return@launch
                    }
                }

                val updateData = mapOf(
                    "bio" to bio,
                    "profilePictureUrl" to avatarUrl,
                    "bannerUrl" to bannerUrl
                )

                databaseRepository.updateUser(currentUser.id, updateData).collect { result ->
                    _updateProfileState.value = result
                    if (result is Resource.Success) {
                        loadUserProfile()
                    }
                }

            } catch (e: Exception) {
                _updateProfileState.value = Resource.Error(e.message ?: "Error inesperado al actualizar perfil")
            }
        }
    }

    fun resetUpdateState() {
        _updateProfileState.value = null
    }
}