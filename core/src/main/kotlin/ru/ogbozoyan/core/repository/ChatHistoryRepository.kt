package ru.ogbozoyan.core.repository

import ru.ogbozoyan.core.model.ChatMessage
import java.util.*

/**
 * @author ogbozoyan
 * @since 24.11.2024
 */
interface ChatHistoryRepository {
    fun upsert(chatMessage: ChatMessage): Int
    fun findByChatId(chatId: UUID): List<ChatMessage>
}