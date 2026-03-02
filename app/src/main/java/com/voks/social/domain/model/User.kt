package com.voks.social.domain.model

data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val profilePictureUrl: String = "",
    val bannerUrl: String = "", // NUEVO FASE 10: Campo para el banner
    val bio: String = "",
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val createdAt: String = ""
)