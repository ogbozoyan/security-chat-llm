package ru.ogbozoyan.core.service.pg

import org.springframework.context.ApplicationEvent
import org.springframework.core.io.Resource
import ru.ogbozoyan.core.service.pg.FileTypeEnum

data class PgEvent(val resource: Resource, val type: FileTypeEnum, val fileName: String?) : ApplicationEvent(resource)