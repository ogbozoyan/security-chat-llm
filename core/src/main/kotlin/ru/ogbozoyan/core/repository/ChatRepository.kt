package ru.ogbozoyan.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.ogbozoyan.core.model.Chat
import java.util.*

@Repository
interface ChatRepository : JpaRepository<Chat, UUID> {

    fun findByUserId(userId: String): List<Chat>

}