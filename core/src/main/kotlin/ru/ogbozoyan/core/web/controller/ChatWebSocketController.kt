package ru.ogbozoyan.core.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import reactor.core.publisher.Flux
import ru.ogbozoyan.core.service.ollama.OllamaService
import ru.ogbozoyan.core.web.dto.ApiRequest
import ru.ogbozoyan.core.web.dto.StreamApiResponse

@Controller
@CrossOrigin(origins = ["*"])
@Tag(name = "WebSocket API", description = "API for WebSocket communication with the server.")
class ChatWebSocketController(
    val ollamaService: OllamaService,
) {
    @Operation(
        summary = "Stream messages via WebSocket",
        description = "Sends a message to the server and streams the response via WebSocket.",
        responses = [
            ApiResponse(
                description = "Streamed responses from the server",
                content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = StreamApiResponse::class))
                ]
            )
        ]
    )
    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    fun streamMessages(request: ApiRequest): Flux<StreamApiResponse> {
        return ollamaService.chatStreaming(request)
    }
}
