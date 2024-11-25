package ru.ogbozoyan.core.repository

import ru.ogbozoyan.core.model.Chat
import java.util.*

interface ChatRepository {
    fun findByUserId(userId: String): List<Chat>
    fun upsert(chat: Chat): UUID
    fun findAll(): List<Chat>
}