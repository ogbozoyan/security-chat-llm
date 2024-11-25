package ru.ogbozoyan.core.model

import java.time.LocalDateTime
import java.util.*

data class Chat(
    val chatId: UUID,
    val userId: String,
    val title: String,
    val createdAt: LocalDateTime
)