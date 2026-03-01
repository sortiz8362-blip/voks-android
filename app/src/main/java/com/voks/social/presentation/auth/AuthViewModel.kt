package com.voks.social.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voks.social.core.utils.Resource
import com.voks.social.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.models.Session
import io.appwrite.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<Session>?>(null)
    val authState: StateFlow<Resource<Session>?> = _authState.asStateFlow()

    private val _registerState = MutableStateFlow<Resource<User<Map<String, Any>>>?>(null)
    val registerState: StateFlow<Resource<User<Map<String, Any>>>?> = _registerState.asStateFlow()

    private val _userState = MutableStateFlow<Resource<User<Map<String, Any>>>?>(null)
    val userState: StateFlow<Resource<User<Map<String, Any>>>?> = _userState.asStateFlow()

    private val _verificationState = MutableStateFlow<Resource<Unit>?>(null)
    val verificationState: StateFlow<Resource<Unit>?> = _verificationState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            repository.login(email, password).collect { result ->
                _authState.value = result
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            repository.register(name, email, password).collect { result ->
                _registerState.value = result
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout().collect {
                _authState.value = null
                _userState.value = null
            }
        }
    }

    fun checkUser() {
        viewModelScope.launch {
            repository.getUser().collect { result ->
                _userState.value = result
            }
        }
    }

    fun sendVerificationEmail() {
        viewModelScope.launch {
            // Actualizado para usar tu nuevo subdominio
            val redirectUrl = "https://voks.saov.page/"
            repository.sendVerificationEmail(redirectUrl).collect { result ->
                _verificationState.value = result
            }
        }
    }

    fun clearStates() {
        _authState.value = null
        _registerState.value = null
        _verificationState.value = null
    }
}