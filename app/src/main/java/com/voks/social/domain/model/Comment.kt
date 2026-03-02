package com.voks.social.domain.model

data class Comment(
    val id: String = "",
    val postId: String = "", // ID del post al que pertenece el comentario
    val userId: String = "", // ID del autor del comentario
    val content: String = "",
    val createdAt: String = ""
)