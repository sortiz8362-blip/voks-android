package com.voks.social.core.utils

/**
 * Clase sellada para manejar el estado de las peticiones a Appwrite
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}