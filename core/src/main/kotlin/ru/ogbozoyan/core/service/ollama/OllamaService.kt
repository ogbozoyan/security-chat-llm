package ru.ogbozoyan.core.service.ollama

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY
import org.springframework.stereotype.Service
import ru.ogbozoyan.core.web.dto.ApiRequest
import ru.ogbozoyan.core.web.dto.ApiResponse


@Service
class OllamaService(
    private val ollamaChat: ChatClient,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass.simpleName)

    fun chat(request: ApiRequest): ApiResponse {
        log.info("TODO")
        return ApiResponse(
            ollamaChat
                .prompt(request.question)
                .advisors(
                    { advisorSpec ->
                        advisorSpec
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY, request.conversationId)
                    }
                )
                .call()
                .content()
        )
    }

}