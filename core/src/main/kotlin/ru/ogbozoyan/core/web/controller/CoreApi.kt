package ru.ogbozoyan.core.web.controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux
import ru.ogbozoyan.core.configuration.MOCK_USER_ID
import ru.ogbozoyan.core.model.Chat
import ru.ogbozoyan.core.model.ChatHistory
import ru.ogbozoyan.core.service.ingestion.FileTypeEnum
import ru.ogbozoyan.core.web.dto.ApiRequest
import ru.ogbozoyan.core.web.dto.ApiResponse
import ru.ogbozoyan.core.web.dto.StreamApiResponse

interface CoreApi {
    @PostMapping(
        "/api/v1/query",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(summary = "Ask question to llm and save chat", description = "Returns the result")
    @ResponseStatus(HttpStatus.OK)
    fun query(@RequestBody request: ApiRequest): ResponseEntity<ApiResponse>

    @PostMapping(
        "/api/v1/embed-file",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Ask question to llm and save chat", description = "Returns the result")
    fun embedFile(file: MultipartFile, type: FileTypeEnum)

    @PostMapping(
        "/api/v1/chat",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    @Operation(summary = "Ask question to llm and save chat (STREAM)", description = "Returns future of parts")
    @ResponseStatus(HttpStatus.OK)
    fun streamMessages(request: ApiRequest): Flux<StreamApiResponse>

    @GetMapping(
        "/api/v1/chat/mock-user",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        summary = "Return chats of user with id $MOCK_USER_ID",
        description = "Return chats of user with id $MOCK_USER_ID"
    )
    @ResponseStatus(HttpStatus.OK)
    fun getChatsByUser(): ResponseEntity<List<Chat>>

    @GetMapping(
        "/api/v1/chat/{chatId}/messages/",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        summary = "Return chat messages by chatId",
        description = "Return chat messages by chatId"
    )
    @ResponseStatus(HttpStatus.OK)
    fun getChatsMessagesByChatId(@PathVariable(value = "chatId") chatId: String): ResponseEntity<List<ChatHistory>>
}