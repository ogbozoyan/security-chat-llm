package ru.ogbozoyan.core.model

import java.util.*

data class ChatHistory(val chatId: UUID, val messageId: UUID, val query: String, val answer: String)