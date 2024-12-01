package ru.ogbozoyan.core.web.dto

import java.util.*

data class StreamApiResponse(val id: Long, val content: String, val isFinal: Boolean, val messageId: Long, val chatId: UUID)


