package ru.ogbozoyan.core.model

import java.time.LocalDateTime
import java.util.*

data class Chat(
    val chatId: UUID,
    val userId: String,
    var title: String,
    val createdAt: LocalDateTime
)