package ru.ogbozoyan.core.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import ru.ogbozoyan.core.model.Chat
import java.util.*

@Repository
class ChatRepositoryImpl(private val jdbcTemplate: JdbcTemplate) : ChatRepository {
    private val rowMapper = RowMapper<Chat> { rs, _ ->
        Chat(
            chatId = UUID.fromString(rs.getString("chat_id")),
            userId = rs.getString("user_id"),
            title = rs.getString("title"),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime()
        )
    }

    override fun findByUserId(userId: String): List<Chat> {
        val sql = "SELECT * FROM chat WHERE user_id = ? ORDER BY created_at DESC"
        return jdbcTemplate.query(sql, rowMapper, userId)
    }

    override fun findAll(): List<Chat> {
        val sql = "SELECT * FROM chat"
        return jdbcTemplate.query(sql, rowMapper)
    }

    override fun upsert(chat: Chat): UUID {
        val sql =
            """
            INSERT INTO chat (chat_id, user_id, title, created_at) 
                VALUES (?, ?, ?, ?) 
                ON CONFLICT (chat_id)
                DO UPDATE SET 
                user_id = EXCLUDED.user_id,
                title = EXCLUDED.title,
                created_at = EXCLUDED.created_at
            RETURNING chat_id
            """
        return jdbcTemplate.queryForObject(
            sql,
            UUID::class.java,
            chat.chatId,
            chat.userId,
            chat.title,
            chat.createdAt
        )
    }
}