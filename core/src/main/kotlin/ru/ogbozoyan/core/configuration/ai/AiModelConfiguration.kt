package ru.ogbozoyan.core.configuration.ai

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.DEFAULT_CHAT_MEMORY_CONVERSATION_ID
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.memory.InMemoryChatMemory
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.evaluation.FactCheckingEvaluator
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.ai.ollama.api.OllamaApi
import org.springframework.ai.ollama.api.OllamaOptions
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.SimpleVectorStore
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource


@Configuration
class AiModelConfiguration(
    private val chatClientBuilder: ChatClient.Builder,
    @Value("\${spring.ai.ollama.chat.options.model}") private val model: String,
    @Value("classpath:/prompts/system-message.st") private val systemMessage: Resource,
    private val vectorStore: VectorStore
//    @Value("classpath:/prompts/prompt-checking-message.st") private val safetyPrompt: Resource,
) {

    @Bean
    fun ollamaClient(): ChatClient {
        return chatClientBuilder
            .defaultSystem(systemMessage)
            .defaultAdvisors(
                MessageChatMemoryAdvisor(inMemoryChatMemory(), DEFAULT_CHAT_MEMORY_CONVERSATION_ID, 5),
//                VectorStoreChatMemoryAdvisor(vectorStore, DEFAULT_CHAT_MEMORY_CONVERSATION_ID, 5),
                QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()), // RAG advisor
//                PromptSafeCheckAdvisor(promptSafetyClient(), safetyPrompt),
                SimpleLoggerAdvisor()
            )
            .build()
    }

    /*
    * Проверяет на релевантность ответ от AI
    */
    @Bean
    fun relevancyEvaluator(): FactCheckingEvaluator {
        return FactCheckingEvaluator(chatClientBuilder)
    }

    @Bean
    fun simpleVectorStore(embeddingModel: EmbeddingModel): VectorStore {
        return SimpleVectorStore(embeddingModel)
    }

    @Bean
    fun inMemoryChatMemory(): ChatMemory {
        return InMemoryChatMemory()
    }

    fun promptSafetyClient(): ChatClient = ChatClient.builder(
        OllamaChatModel(
            OllamaApi(), OllamaOptions
                .create()
                .withModel(model)
                .withTemperature(0.4)
                .build()
        )
    )
        .defaultAdvisors(
            SimpleLoggerAdvisor()
        )
        .build()
}