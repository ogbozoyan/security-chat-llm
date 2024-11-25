package ru.ogbozoyan.core.repository

import ru.ogbozoyan.core.model.ChatHistory
import java.util.*

/**
 * @author ogbozoyan
 * @since 24.11.2024
 */
interface ChatHistoryRepository {
    fun upsert(chatHistory: ChatHistory): Int
    fun findByChatId(chatId: UUID): List<ChatHistory>
}