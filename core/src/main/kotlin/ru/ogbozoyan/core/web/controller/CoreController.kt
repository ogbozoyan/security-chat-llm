package ru.ogbozoyan.core.web.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.ogbozoyan.core.service.ollama.OllamaService
import ru.ogbozoyan.core.service.ingestion.FileTypeEnum
import ru.ogbozoyan.core.service.ingestion.IngestionEvent
import ru.ogbozoyan.core.web.dto.ApiRequest
import ru.ogbozoyan.core.web.dto.ApiResponse


@RestController
@CrossOrigin(origins = ["*"])
@Tag(name = "Core API controller", description = "API")
class CoreController(
    val publisher: ApplicationEventPublisher,
    private val ollamaService: OllamaService
) : CoreApi {

    private val log = LoggerFactory.getLogger(CoreController::class.java)

    override fun query(@RequestBody request: ApiRequest): ResponseEntity<ApiResponse> {
        return ResponseEntity.ok(ollamaService.chat(request))
    }

    override fun embedFile(
        @RequestPart("file", required = true) file: MultipartFile,
        @RequestParam("type", required = true) type: FileTypeEnum
    ) {
        return try {
            log.info("Event triggered via REST endpoint for file: ${file.originalFilename} with type: $type")
            val byteArrayResource = ByteArrayResource(file.bytes)
            publisher.publishEvent(IngestionEvent(byteArrayResource, type, file.originalFilename))
        } catch (e: Exception) {
            log.error("Error triggering event: {}", e.message)
        }
    }

}