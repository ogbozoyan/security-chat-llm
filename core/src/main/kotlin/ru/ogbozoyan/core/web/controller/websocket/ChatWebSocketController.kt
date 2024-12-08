package ru.ogbozoyan.core.web.controller.websocket

import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import ru.ogbozoyan.core.service.ollama.OllamaService
import ru.ogbozoyan.core.web.dto.ApiRequest
import ru.ogbozoyan.core.web.dto.StreamApiResponse


@Controller
class ChatWebSocketController(
    private val ollamaService: OllamaService,
    private val messagingTemplate: SimpMessagingTemplate
) {
    private val log = LoggerFactory.getLogger(ChatWebSocketController::class.java)

    @MessageMapping("/chat") // Клиент отправляет сообщения сюда
    fun handleChatRequest(request: ApiRequest) {
        log.info("Received WebSocket request for conversationId=${request.conversationId}: ${request.question}")

        ollamaService.chatStreaming(request)
            .doOnNext { part ->
                messagingTemplate.convertAndSend(
                    "/topic/messages/${request.conversationId}",
                    part
                )
            }
            .doOnError { error ->
                log.error("Error during chatStreaming: ${error.message}", error)
                messagingTemplate.convertAndSend(
                    "/topic/messages/${request.conversationId}",
                    StreamApiResponse(
                        Long.MAX_VALUE,
                        "Error while generating response, please try again",
                        true,
                        Long.MAX_VALUE,
                        chatId = request.conversationId
                    )

                )
            }
            .subscribe()
    }
}