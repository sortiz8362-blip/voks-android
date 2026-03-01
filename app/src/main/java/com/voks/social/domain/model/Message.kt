package com.voks.social.domain.model

data class Message(
    val id: String = "",
    val senderId: String = "", // Quién envía el mensaje
    val receiverId: String = "", // REGLA INQUEBRANTABLE: Un único receptor (Chat 1 a 1)
    val content: String = "",
    val isRead: Boolean = false,
    val createdAt: String = ""
)