package ru.ogbozoyan.core.service.ollama

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import ru.ogbozoyan.core.service.chat.ChatService
import ru.ogbozoyan.core.web.dto.ApiRequest
import ru.ogbozoyan.core.web.dto.ApiResponse


@Service
class OllamaService(
    @Qualifier("ollamaClient") private val ollamaChat: ChatClient,
    private val chatService: ChatService
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass.simpleName)

    fun chat(request: ApiRequest): ApiResponse {
        val chatHistory = chatService.getMessagesForChat(request.conversationId)
        log.info("Fetched chat history for conversationId=${request.conversationId}. Message count: ${chatHistory.size}")

        if (chatHistory.isEmpty()) {
            createNewChatWithConversationId(request)
        } else {
            chatService.saveMessage(request.conversationId, true, request.question)
        }

        val responseContent = try {
            ollamaChat
                .prompt(getPrompt(request))
                .advisors { advisorSpec ->
                    advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, request.conversationId)
                }
                .call()
                .content()
        } catch (e: Exception) {
            log.error("Error while calling AI for conversationId=${request.conversationId}: ${e.message}", e)
            throw e
        }

        if (responseContent == null) {
            log.error("AI response was null for conversationId=${request.conversationId}")
            throw IllegalStateException("AI response content cannot be null")
        }

        chatService.saveMessage(request.conversationId, false, responseContent)
        log.info("AI response saved to chat history for conversationId=${request.conversationId}")

        return ApiResponse(responseContent)
    }

    private fun createNewChatWithConversationId(request: ApiRequest) {
        log.info("Creating a new chat for conversationId=${request.conversationId}")

        chatService.createChat(request.question, request.conversationId)
        log.info("Chat created with title: ${request.question} for conversationId=${request.conversationId}")

        chatService.saveMessage(request.conversationId, true, request.question)
        log.info("User's initial message saved for conversationId=${request.conversationId}")
    }

    private fun getPrompt(request: ApiRequest): Prompt {
        log.info("Generating prompt for user query: ${request.question}")
        return Prompt(listOf(UserMessage(request.question)))
    }
}