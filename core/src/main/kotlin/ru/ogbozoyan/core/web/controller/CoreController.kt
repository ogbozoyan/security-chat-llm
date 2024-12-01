package ru.ogbozoyan.core.web.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux
import ru.ogbozoyan.core.configuration.MOCK_USER_ID
import ru.ogbozoyan.core.model.Chat
import ru.ogbozoyan.core.model.ChatHistory
import ru.ogbozoyan.core.service.chat.ChatService
import ru.ogbozoyan.core.service.ingestion.FileTypeEnum
import ru.ogbozoyan.core.service.ingestion.IngestionEvent
import ru.ogbozoyan.core.service.ollama.OllamaService
import ru.ogbozoyan.core.web.dto.ApiRequest
import ru.ogbozoyan.core.web.dto.ApiResponse
import ru.ogbozoyan.core.web.dto.StreamApiResponse
import java.util.*


@RestController
@CrossOrigin(origins = ["*"])
@Tag(name = "Core API controller", description = "API")
class CoreController(
    val publisher: ApplicationEventPublisher,
    private val ollamaService: OllamaService,
    private val chatService: ChatService
) : CoreApi {

    private val log = LoggerFactory.getLogger(CoreController::class.java)

    override fun query(@RequestBody request: ApiRequest): ResponseEntity<ApiResponse> =
        ResponseEntity.ok(ollamaService.chat(request))


    override fun streamMessages(@RequestBody request: ApiRequest): Flux<StreamApiResponse> =
        ollamaService.chatStreaming(request)

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

    override fun getChatsByUser(): ResponseEntity<List<Chat>> =
        ResponseEntity.ok(chatService.getChatsForUser(MOCK_USER_ID))

    override fun getChatsMessagesByChatId(chatId: String): ResponseEntity<List<ChatHistory>> = ResponseEntity.ok(
        chatService.getMessagesForChat(
            UUID.fromString(chatId)
        )
    )
}