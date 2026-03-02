package com.voks.social.domain.model

data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val profilePictureUrl: String = "",
    val bannerUrl: String = "",
    val bio: String = "",
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val bookmarks: List<String> = emptyList(), // NUEVO FASE 13: Posts guardados
    val createdAt: String = ""
)