package ru.ogbozoyan.core.service.ollama

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.ogbozoyan.core.model.ChatHistory
import ru.ogbozoyan.core.service.chat.ChatService
import ru.ogbozoyan.core.web.dto.ApiRequest
import ru.ogbozoyan.core.web.dto.ApiResponse
import ru.ogbozoyan.core.web.dto.StreamApiResponse
import java.util.concurrent.atomic.AtomicLong


@Suppress("BlockingMethodInNonBlockingContext")
@Service
class OllamaService(
    @Qualifier("ollamaClient") private val ollamaChat: ChatClient,
    private val chatService: ChatService,
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

            return ApiResponse(answer = responseContent)

        } catch (e: Exception) {
            log.error("Error while calling AI for conversationId=${request.conversationId}: ${e.message}", e)
            throw e
        }
    }

    fun chatStreaming(request: ApiRequest): Flux<StreamApiResponse> {
        val messageAccumulator = StringBuilder()

        val chatHistory = chatService.getMessagesForChat(request.conversationId)
        log.info("Fetched chat history for conversationId=${request.conversationId}. Message count: ${chatHistory.size}")

        if (chatHistory.isEmpty()) {
            createNewChatWithConversationId(request)
        } else {
            chatService.saveMessage(request.conversationId, true, request.question)
        }

        val nextMessageId = chatService.getNextMessageId()
        val partId: AtomicLong = AtomicLong(0)
        return ollamaChat
            .prompt(getPrompt(request))
            .advisors { advisorSpec ->
                advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, request.conversationId)
            }
            .stream()
            .content()
            .map { part ->
                log.debug("Received part of response: $part")

                messageAccumulator.append(part)

                StreamApiResponse(
                    id = partId.getAndIncrement(),
                    content = part,
                    isFinal = false,
                    messageId = nextMessageId,
                    chatId = request.conversationId
                )
            }
            .concatWith(
                Mono.defer {

                    val fullMessage = messageAccumulator.toString()

                    log.info("Full message saved with messageId=$nextMessageId for conversationId=${request.conversationId}")

                    Mono.just(
                        StreamApiResponse(
                            id = partId.getAndIncrement(),
                            content = "Message saved successfully",
                            isFinal = true,
                            chatId = request.conversationId,
                            messageId = chatService.saveMessage(
                                request.conversationId,
                                isUser = false,
                                content = fullMessage,
                                messageId = nextMessageId
                            )
                                .messageId!!,
                        )
                    )
                }
            )
            .doOnError { e ->
                log.error("Error during streaming for conversationId=${request.conversationId}: ${e.message}", e)
                throw e
            }
    }

    private fun createNewChatWithConversationId(request: ApiRequest): ChatHistory {
        log.info("Creating a new chat for conversationId=${request.conversationId}")

        chatService.createChat(request.question, request.conversationId)
        log.info("Chat created with title: ${request.question} for conversationId=${request.conversationId}")

        return chatService.saveMessage(request.conversationId, true, request.question)
    }

    private fun getPrompt(request: ApiRequest): Prompt {
        log.info("Generating prompt for user query: ${request.question}")
        return Prompt(listOf(UserMessage(request.question)))
    }
}