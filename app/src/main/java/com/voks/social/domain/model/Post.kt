package com.voks.social.domain.model

data class Post(
    val id: String = "",
    val userId: String = "", // ID del autor del post (o de quien hace el repost)
    val content: String = "",
    val imageUrl: String = "", // Estará vacío si es solo texto. Aquí guardaremos la URL del Storage
    val likes: List<String> = emptyList(), // Lista de IDs que dieron like
    val reposts: List<String> = emptyList(), // NUEVO FASE 15: Usuarios que han reposteado
    val originalPostId: String = "", // NUEVO FASE 15: ID del post original si es un repost o cita
    val createdAt: String = ""
)