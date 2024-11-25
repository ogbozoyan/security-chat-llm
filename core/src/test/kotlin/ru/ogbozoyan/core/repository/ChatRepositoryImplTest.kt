package ru.ogbozoyan.core.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import ru.ogbozoyan.core.model.Chat
import java.time.LocalDateTime
import java.util.*

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChatRepositoryImplTest {

    @Autowired
    var jdbcTemplate: JdbcTemplate? = null

    lateinit var chatRepository: ChatRepository

    @BeforeEach
    fun setUp() {
        chatRepository = ChatRepositoryImpl(jdbcTemplate!!)
    }

    @Test
    fun `test upsert inserts or updates chat`() {
        val userId = "user-123"
        val chat = Chat(
            chatId = UUID.randomUUID(),
            userId = userId,
            title = "Test Chat",
            createdAt = LocalDateTime.now()
        )
        val savedChatId = chatRepository.upsert(chat)
        val findByUserId = chatRepository.findByUserId(userId)

        assertNotNull(savedChatId)
        assertThat(findByUserId).isNotEmpty
        assertThat(findByUserId.get(0)).isEqualTo(chat)
    }
}