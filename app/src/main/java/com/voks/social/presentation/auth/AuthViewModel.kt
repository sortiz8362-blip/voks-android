package com.voks.social.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voks.social.core.utils.Resource
import com.voks.social.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<Unit>?>(null)
    val authState: StateFlow<Resource<Unit>?> = _authState.asStateFlow()

    // Nuevo estado para comprobar la sesión al inicio
    private val _isUserLoggedIn = MutableStateFlow<Resource<Boolean>?>(null)
    val isUserLoggedIn: StateFlow<Resource<Boolean>?> = _isUserLoggedIn.asStateFlow()

    fun checkAuthStatus() {
        viewModelScope.launch {
            repository.checkAuthStatus().collect { result ->
                _isUserLoggedIn.value = result
            }
        }
    }

    fun login(email: String, clave: String) {
        viewModelScope.launch {
            repository.login(email, clave).collect { result ->
                when (result) {
                    is Resource.Loading -> _authState.value = Resource.Loading
                    is Resource.Success -> _authState.value = Resource.Success(Unit)
                    is Resource.Error -> _authState.value = Resource.Error(result.message)
                }
            }
        }
    }

    fun register(name: String, email: String, clave: String) {
        viewModelScope.launch {
            repository.register(name, email, clave).collect { result ->
                when (result) {
                    is Resource.Loading -> _authState.value = Resource.Loading
                    is Resource.Success -> _authState.value = Resource.Success(Unit)
                    is Resource.Error -> _authState.value = Resource.Error(result.message)
                }
            }
        }
    }

    fun resetAuthState() {
        _authState.value = null
    }
}