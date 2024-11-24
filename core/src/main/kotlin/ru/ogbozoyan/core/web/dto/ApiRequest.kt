package ru.ogbozoyan.core.web.dto

import java.util.UUID

data class ApiRequest(val question: String, val conversationId: UUID)