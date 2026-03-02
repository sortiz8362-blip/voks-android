package com.voks.social.presentation.profile

import android.net.Uri
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
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<Resource<User>>(Resource.Loading)
    val userProfile: StateFlow<Resource<User>> = _userProfile.asStateFlow()

    private val _userPosts = MutableStateFlow<Resource<List<Post>>>(Resource.Loading)
    val userPosts: StateFlow<Resource<List<Post>>> = _userPosts.asStateFlow()

    private val _updateProfileState = MutableStateFlow<Resource<Unit>?>(null)
    val updateProfileState: StateFlow<Resource<Unit>?> = _updateProfileState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            authRepository.getUser().collect { resource ->
                if (resource is Resource.Success) {
                    val userId = resource.data.id
                    databaseRepository.getUser(userId).collect { userResource ->
                        _userProfile.value = userResource
                        if (userResource is Resource.Success) {
                            loadUserPosts(userId)
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
                    // Filtramos para mostrar únicamente los posts creados por este usuario
                    val filteredPosts = resource.data.filter { it.userId == userId }
                    _userPosts.value = Resource.Success(filteredPosts)
                } else if (resource is Resource.Error) {
                    _userPosts.value = Resource.Error(resource.message ?: "Error al cargar posts del usuario")
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

                // 1. Subir nuevo Avatar si se seleccionó
                if (newAvatarUri != null) {
                    val avatarUpload = storageRepository.uploadMedia(newAvatarUri, Constants.PROFILE_IMAGES_BUCKET_ID)
                    if (avatarUpload is Resource.Success) {
                        avatarUrl = avatarUpload.data
                    } else {
                        _updateProfileState.value = Resource.Error("Error al subir foto de perfil")
                        return@launch
                    }
                }

                // 2. Subir nuevo Banner si se seleccionó
                if (newBannerUri != null) {
                    val bannerUpload = storageRepository.uploadMedia(newBannerUri, Constants.PROFILE_IMAGES_BUCKET_ID)
                    if (bannerUpload is Resource.Success) {
                        bannerUrl = bannerUpload.data
                    } else {
                        _updateProfileState.value = Resource.Error("Error al subir banner")
                        return@launch
                    }
                }

                // 3. Actualizar la base de datos
                val updateData = mapOf(
                    "bio" to bio,
                    "profilePictureUrl" to avatarUrl,
                    "bannerUrl" to bannerUrl
                )

                databaseRepository.updateUser(currentUser.id, updateData).collect { result ->
                    _updateProfileState.value = result
                    if (result is Resource.Success) {
                        loadUserProfile() // Recargar datos locales tras guardar
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