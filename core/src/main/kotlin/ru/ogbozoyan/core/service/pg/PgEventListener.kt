package ru.ogbozoyan.core.service.pg

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class PgEventListener(private val vectorStore: PGVectorStoreService) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Async("asyncThreadPoolExecutor")
    @EventListener(PgEvent::class)
    fun process(event: PgEvent) {
        log.info("Processing event: $event")
        when (event.type) {
            FileTypeEnum.PDF -> {
                log.info("PDF processing event: $event")
                vectorStore.saveNewPDFAsync(event.resource, event.fileName)
            }

            FileTypeEnum.TXT -> {
                log.info("TXT processing event: $event")
                vectorStore.saveNewTextAsync(event.resource, event.fileName)

            }

            FileTypeEnum.MD -> {
                log.info("MD processing event: $event")
                vectorStore.saveNewTextAsync(event.resource, event.fileName)
            }
        }
    }
}

