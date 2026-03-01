package com.voks.social.domain.model

data class Post(
    val id: String = "",
    val userId: String = "", // ID del autor del post
    val content: String = "",
    val imageUrl: String = "", // Estará vacío si es solo texto
    val likes: List<String> = emptyList(), // Lista de IDs que dieron like
    val createdAt: String = ""
)