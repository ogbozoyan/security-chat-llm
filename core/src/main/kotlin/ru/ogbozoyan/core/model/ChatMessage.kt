package ru.ogbozoyan.core.model

import java.time.LocalDateTime
import java.util.*

data class ChatMessage(
    val messageId: Long?,
    val chatId: UUID,
    val isUser: Boolean,
    val content: String,
    val createdAt: LocalDateTime
)