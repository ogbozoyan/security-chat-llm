package ru.ogbozoyan.core.service.ollama

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.ogbozoyan.core.service.chat.ChatService
import ru.ogbozoyan.core.web.dto.ApiRequest
import ru.ogbozoyan.core.web.dto.ApiResponse


@Service
class OllamaService(
    @Qualifier("ollamaClient") private val ollamaChat: ChatClient,
    @Qualifier("ollamaWithoutRAG") private val ollamaWithoutRAG: ChatClient,
    private val chatService: ChatService,
    private val applicationScope: CoroutineScope
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass.simpleName)

    fun chat(request: ApiRequest): ApiResponse {
        try {
            val chatHistory = chatService.getMessagesForChat(request.conversationId)
            log.info("Fetched chat history for conversationId=${request.conversationId}. Message count: ${chatHistory.size}")

            if (chatHistory.isEmpty()) {
                createNewChatWithConversationId(request)
            } else {
                chatService.saveMessage(request.conversationId, true, request.question)
            }

            val responseContent =
                ollamaChat
                    .prompt(getPrompt(request))
                    .advisors { advisorSpec ->
                        advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, request.conversationId)
                    }
                    .call()
                    .content()

            if (responseContent == null) {
                log.error("AI response was null for conversationId=${request.conversationId}")
                throw IllegalStateException("AI response content cannot be null")
            }

            chatService.saveMessage(request.conversationId, false, responseContent)
            log.info("AI response saved to chat history for conversationId=${request.conversationId}")

            return ApiResponse(responseContent)

        } catch (e: Exception) {
            log.error("Error while calling AI for conversationId=${request.conversationId}: ${e.message}", e)
            throw e
        }
    }

    @Scheduled(cron = "0 30 18 * * *")
    fun renameChat() {
        applicationScope.launch {
            log.info("Starting chat renaming process")
            try {
                val allChats = chatService.getAllChats()

                for (chat in allChats) {
                    log.info("Processing chat: ${chat.chatId}")

                    val messagesForChat =
                        chatService.getMessagesForChat(chat.chatId)

                    val stringBuilder = buildString {
                        append("Give me topic based on this text: ")

                        append(
                            messagesForChat.map { message -> message.content }.joinToString { ", " }.let {
                                if (it.length >= 1000) {
                                    it.drop(1000)
                                } else {
                                    it
                                }
                            }.toString()
                        )
                    }

                    if (stringBuilder.isNotBlank()) {

                        val topicFromAi = ollamaWithoutRAG
                            .prompt(stringBuilder)
                            .call()
                            .content()!!

                        log.info("New topic generated for chat ${chat.chatId}: $topicFromAi")
                        chat.title = topicFromAi

                        chatService.updateChat(chat)
                    } else {
                        chat.title
                    }

                    log.info("Updated chat ${chat.chatId}")
                }
            } catch (e: Exception) {
                log.error("Error during chat renaming process", e)
            }
        }
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