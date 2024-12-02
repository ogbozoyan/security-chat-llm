package ru.ogbozoyan.core.service.ingestion

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import ru.ogbozoyan.core.model.ContentTypeEnum

@Component
class IngestionEventListener(
    private val vectorStore: IngestionService,
    private val applicationScope: CoroutineScope
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener(IngestionEvent::class)
    fun process(event: IngestionEvent) {
        applicationScope.launch {
            log.info("Processing event: ${event.fileName}")

            when (event.type) {
                ContentTypeEnum.PDF -> {
                    log.info("PDF processing event: $event")
                    vectorStore.saveNewPDFAsync(event.resource, event.fileName, event.chatId)
                }

                ContentTypeEnum.TXT -> {
                    log.info("TXT processing event: $event")
                    vectorStore.saveNewTextAsync(event.resource, event.fileName, event.chatId)

                }

                ContentTypeEnum.MD -> {
                    log.info("MD processing event: $event")
                    vectorStore.saveNewTextAsync(event.resource, event.fileName, event.chatId)
                }

                ContentTypeEnum.PLAIN -> {}
            }
        }
    }
}

