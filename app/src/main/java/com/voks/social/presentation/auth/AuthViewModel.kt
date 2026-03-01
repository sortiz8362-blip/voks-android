package com.voks.social.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voks.social.core.utils.Resource
import io.appwrite.models.User
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

    private val _userState = MutableStateFlow<Resource<User<Map<String, Any>>>?>(null)
    val userState: StateFlow<Resource<User<Map<String, Any>>>?> = _userState.asStateFlow()

    private val _passwordRecoveryState = MutableStateFlow<Resource<Unit>?>(null)
    val passwordRecoveryState: StateFlow<Resource<Unit>?> = _passwordRecoveryState.asStateFlow()

    fun checkUser() {
        viewModelScope.launch {
            _userState.value = Resource.Loading
            repository.getUser().collect { result ->
                _userState.value = result
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading

            try {
                repository.logout().collect { }
            } catch (e: Exception) { /* Ignorar si no había sesión */ }

            repository.login(email, password).collect { result ->
                _authState.value = when (result) {
                    is Resource.Success -> Resource.Success(Unit)
                    is Resource.Error -> Resource.Error(result.message)
                    is Resource.Loading -> Resource.Loading
                }
            }
        }
    }

    fun register(email: String, password: String, name: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading

            try {
                repository.logout().collect { }
            } catch (e: Exception) { /* Ignorar si no había sesión */ }

            // 1. Creamos la cuenta
            repository.register(name = name, email = email, password = password).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        // 2. Cuenta creada. Ahora DEBEMOS iniciar sesión para poder mandar el correo
                        repository.login(email, password).collect { loginResult ->
                            when (loginResult) {
                                is Resource.Success -> {
                                    // 3. Sesión iniciada. Ahora SÍ podemos enviar el correo de verificación
                                    repository.sendVerificationEmail("https://voks.saov.page/").collect { verifyResult ->
                                        _authState.value = verifyResult
                                    }
                                }
                                is Resource.Error -> {
                                    _authState.value = Resource.Error(loginResult.message)
                                }
                                is Resource.Loading -> {
                                    // Mantenemos el estado de carga
                                }
                            }
                        }
                    }
                    is Resource.Error -> {
                        _authState.value = Resource.Error(result.message)
                    }
                    is Resource.Loading -> {
                        _authState.value = Resource.Loading
                    }
                }
            }
        }
    }

    fun sendPasswordRecoveryEmail(email: String) {
        viewModelScope.launch {
            _passwordRecoveryState.value = Resource.Loading
            repository.sendPasswordRecoveryEmail(email, "https://voks.saov.page/reset-password").collect { result ->
                _passwordRecoveryState.value = result
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                repository.logout().collect { }
            } catch (e: Exception) { /* Ignorar */ }
            _userState.value = null
            _authState.value = null
        }
    }

    fun clearStates() {
        _authState.value = null
        _passwordRecoveryState.value = null
    }
}