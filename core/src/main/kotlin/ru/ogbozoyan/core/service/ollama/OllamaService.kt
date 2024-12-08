package ru.ogbozoyan.core.service.ollama

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.ogbozoyan.core.configuration.ai.AiRagAdvisorFactory
import ru.ogbozoyan.core.service.chat.ChatService
import ru.ogbozoyan.core.web.dto.ApiRequest
import ru.ogbozoyan.core.web.dto.ApiResponse
import ru.ogbozoyan.core.web.dto.StreamApiResponse
import java.util.concurrent.atomic.AtomicLong


@Service
class OllamaService(
    @Qualifier("ollamaClient") private val ollamaChat: ChatClient,
    private val chatService: ChatService,
    private val aiRagAdvisorFactory: AiRagAdvisorFactory,
) {
    private val log: Logger = LoggerFactory.getLogger(OllamaService::class.java)

    fun chat(request: ApiRequest): ApiResponse {
        try {

            chatService.preInitChatHistoryIfNotExists(request)

            val responseContent =
                chatClientRequest(request)
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

    @Suppress("LoggingStringTemplateAsArgument", "BlockingMethodInNonBlockingContext")
    fun chatStreaming(request: ApiRequest): Flux<StreamApiResponse> {

        chatService.preInitChatHistoryIfNotExists(request)

        val nextMessageId = chatService.getNextMessageId()
        val partOrder = AtomicLong(0)
        val messageAccumulator = StringBuilder()

        return chatClientRequest(request)
            .stream()
            .content()
            .map { part ->
                log.debug("Received part of response: $part")

                messageAccumulator.append(part)

                StreamApiResponse(
                    partOrder = partOrder.getAndIncrement(),
                    content = part,
                    isFinal = false,
                    messageId = nextMessageId,
                    chatId = request.conversationId
                )
            }
            .concatWith(
                Mono.defer {

                    val fullMessage = messageAccumulator.toString()

                    log.debug("Full message saved with messageId=$nextMessageId for conversationId=${request.conversationId}")

                    Mono.just(
                        StreamApiResponse(
                            partOrder = partOrder.getAndIncrement(),
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

    private fun chatClientRequest(request: ApiRequest): ChatClient.ChatClientRequestSpec {
        val prompt = ollamaChat
            .prompt(getPrompt(request))
            .advisors { advisorSpec ->
                advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, request.conversationId)
                advisorSpec.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)
            }

        if (aiRagAdvisorFactory.enabled()) {
            prompt.advisors(aiRagAdvisorFactory.questionAnswerAdvisor(request.conversationId.toString()))
        }

        return prompt
    }


    private fun getPrompt(request: ApiRequest): Prompt {
        log.info("Generating prompt for user query: ${request.question}")
        return Prompt(listOf(UserMessage(request.question)))
    }
}