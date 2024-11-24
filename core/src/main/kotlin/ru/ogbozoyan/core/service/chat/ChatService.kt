package ru.ogbozoyan.core.service.chat

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import ru.ogbozoyan.core.configuration.MOCK_USER_ID
import ru.ogbozoyan.core.model.Chat
import ru.ogbozoyan.core.model.ChatMessage
import ru.ogbozoyan.core.repository.ChatHistoryRepository
import ru.ogbozoyan.core.repository.ChatRepository
import java.time.LocalDateTime
import java.util.*

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatHistoryRepository: ChatHistoryRepository
) {

    @Transactional(readOnly = true)
    fun getChatsForUser(userId: String): List<Chat> {
        return chatRepository.findByUserId(userId)
    }

    @Transactional(readOnly = true)
    fun getMessagesForChat(chatId: UUID): List<ChatMessage> {
        return chatHistoryRepository.findByChatId(chatId)
    }

    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRES_NEW)
    fun saveMessage(chatId: UUID, isUser: Boolean, content: String): Int {
        val chatMessage = ChatMessage(
            messageId = null,
            chatId = chatId,
            isUser = isUser,
            content = content,
            createdAt = LocalDateTime.now()
        )
        return chatHistoryRepository.upsert(chatMessage)
    }


    @Transactional(rollbackFor = [Exception::class], propagation = Propagation.REQUIRES_NEW)
    fun createChat(title: String, chatId: UUID): UUID {
        val chat = Chat(
            chatId = chatId,
            userId = MOCK_USER_ID,
            title = title,
            createdAt = LocalDateTime.now()
        )
        return chatRepository.upsert(chat)
    }
}