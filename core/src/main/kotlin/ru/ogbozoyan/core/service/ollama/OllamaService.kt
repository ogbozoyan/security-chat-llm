package ru.ogbozoyan.core.service.ollama

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import ru.ogbozoyan.core.web.dto.ApiRequest
import ru.ogbozoyan.core.web.dto.ApiResponse


@Service
class OllamaService(
    private val ollamaChat: ChatClient,
    @Value("classpath:/prompts/system-message-ru.st") private val systemMessage: Resource,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass.simpleName)

    fun chat(request: ApiRequest): ApiResponse {
        return ApiResponse(
            ollamaChat
                .prompt(getPrompt(systemMessage, request))
                .advisors { advisorSpec ->
                    advisorSpec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, request.conversationId)
                }
                .call()
                .content()!!
        )
    }

    private fun getPrompt(systemMessage: Resource, request: ApiRequest): Prompt =
        Prompt(listOf(SystemMessage(systemMessage), UserMessage(request.question)))

}