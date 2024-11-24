package ru.ogbozoyan.core.model

import java.time.LocalDateTime
import java.util.*

data class ChatHistory(
    val chatId: UUID,
    val messageId: UUID,
    val query: String,
    val answer: String,
    val createdAt: LocalDateTime,
    val answeredAt: LocalDateTime,
    val answeredBy: String,
)