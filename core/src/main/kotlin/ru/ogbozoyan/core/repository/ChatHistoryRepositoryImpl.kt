package ru.ogbozoyan.core.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import ru.ogbozoyan.core.model.ChatMessage
import java.util.*

@Repository
class ChatHistoryRepositoryImpl(private val jdbcTemplate: JdbcTemplate) : ChatHistoryRepository {
    private val rowMapper = RowMapper<ChatMessage> { rs, _ ->
        ChatMessage(
            messageId = rs.getLong("message_id"),
            chatId = UUID.fromString(rs.getString("chat_id")),
            isUser = rs.getBoolean("is_user"),
            content = rs.getString("content"),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime()
        )
    }

    override fun findByChatId(chatId: UUID): List<ChatMessage> {
        val sql =
            """
            SELECT * FROM chat_history WHERE chat_id = ? ORDER BY created_at 
            """
        return jdbcTemplate.query(sql, rowMapper, chatId)
    }

    override fun upsert(chatMessage: ChatMessage): Int {

        val sql =
            """
            INSERT INTO chat_history (chat_id, is_user, content, created_at) 
            VALUES ( ?, ?, ?, ?) 
            """

        return jdbcTemplate.update(
            sql,
            chatMessage.chatId,
            chatMessage.isUser,
            chatMessage.content,
            chatMessage.createdAt
        )
    }
}