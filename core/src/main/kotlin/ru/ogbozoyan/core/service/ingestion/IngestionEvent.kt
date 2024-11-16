package ru.ogbozoyan.core.service.ingestion

import org.springframework.context.ApplicationEvent
import org.springframework.core.io.Resource

data class IngestionEvent(val resource: Resource, val type: FileTypeEnum, val fileName: String?) : ApplicationEvent(resource)