package ru.ogbozoyan.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.ogbozoyan.core.model.ChatHistory
import java.util.*

@Repository
interface ChatHistoryRepository : JpaRepository<ChatHistory, Long> {

    @Query("select c from ChatHistory c where c.chat.chatId = :chatId")
    fun findByChatId(chatId: UUID): List<ChatHistory>
}