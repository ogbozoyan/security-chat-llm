package ru.ogbozoyan.core.service.chat

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.ogbozoyan.core.configuration.MOCK_USER_ID
import ru.ogbozoyan.core.model.Chat
import ru.ogbozoyan.core.model.ChatHistory
import ru.ogbozoyan.core.repository.ChatHistoryRepository
import ru.ogbozoyan.core.repository.ChatRepository
import java.time.OffsetDateTime
import java.util.*

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatHistoryRepository: ChatHistoryRepository,
    private val entityManager: EntityManager
) {

    @Transactional(readOnly = true)
    fun getMessagesForChat(chatId: UUID): List<ChatHistory> =
        chatHistoryRepository.findByChatId(chatId)


    @Transactional(rollbackFor = [Exception::class])
    fun saveMessage(chatId: UUID, isUser: Boolean, content: String): ChatHistory {

        val findById: Optional<Chat> = chatRepository.findById(chatId)

        val chatHistory = ChatHistory(
            messageId = getNextMessageId(),
            chat = findById.get(),
            isUser = isUser,
            content = content,
            createdAt = OffsetDateTime.now()
        )
        return chatHistoryRepository.save(chatHistory)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun saveMessage(chatId: UUID, isUser: Boolean, content: String, messageId: Long): ChatHistory {

        val findById: Optional<Chat> = chatRepository.findById(chatId)

        val chatHistory = ChatHistory(
            messageId = messageId,
            chat = findById.get(),
            isUser = isUser,
            content = content,
            createdAt = OffsetDateTime.now()
        )
        return chatHistoryRepository.save(chatHistory)
    }


    @Transactional(rollbackFor = [Exception::class])
    fun createChat(title: String, chatId: UUID): Chat {
        val chat = Chat(
            chatId = chatId,
            userId = MOCK_USER_ID,
            title = title,
            createdAt = OffsetDateTime.now()
        )
        return chatRepository.save(chat)
    }


    @Transactional(rollbackFor = [Exception::class])
    fun getNextMessageId(): Long =
        entityManager.createNativeQuery("SELECT nextval('chat_history_message_id_seq')").singleResult as Long

}