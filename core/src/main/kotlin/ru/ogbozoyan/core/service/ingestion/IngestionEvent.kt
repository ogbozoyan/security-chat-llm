package ru.ogbozoyan.core.service.ingestion

import org.springframework.context.ApplicationEvent
import org.springframework.core.io.Resource
import ru.ogbozoyan.core.model.ContentTypeEnum
import java.util.UUID

data class IngestionEvent(
    val resource: Resource,
    val type: ContentTypeEnum,
    val fileName: String?,
    val chatId: UUID?
) : ApplicationEvent(resource)