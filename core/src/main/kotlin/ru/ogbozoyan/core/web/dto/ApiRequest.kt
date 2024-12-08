package ru.ogbozoyan.core.web.dto

import java.util.*

data class ApiRequest(val question: String, val conversationId: UUID)