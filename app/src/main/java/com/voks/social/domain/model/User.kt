package com.voks.social.domain.model

data class User(
    val id: String = "", // Mapea con el $id autogenerado de Appwrite
    val username: String = "",
    val email: String = "",
    val profilePictureUrl: String = "",
    val bio: String = "",
    val followers: List<String> = emptyList(), // Lista de IDs de usuarios
    val following: List<String> = emptyList(), // Lista de IDs de usuarios
    val createdAt: String = "" // Mapea con el $createdAt de Appwrite
)