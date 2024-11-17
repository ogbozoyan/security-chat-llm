package ru.ogbozoyan.core.configuration

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.DEFAULT_CHAT_MEMORY_CONVERSATION_ID
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.client.advisor.VectorStoreChatMemoryAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.memory.InMemoryChatMemory
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever
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
    @Value("classpath:/prompts/system-message.st") private val systemMessage: Resource,
    private val vectorStore: VectorStore
) {

    @Bean
    fun ollamaClient(): ChatClient {
        val documentRetriever: VectorStoreDocumentRetriever = VectorStoreDocumentRetriever.builder()
            .vectorStore(vectorStore)
            .similarityThreshold(0.50)
            .build()

        return chatClientBuilder
            .defaultSystem(systemMessage)
            .defaultAdvisors(
//                MessageChatMemoryAdvisor(inMemoryChatMemory(), DEFAULT_CHAT_MEMORY_CONVERSATION_ID, 50),
                VectorStoreChatMemoryAdvisor(vectorStore, DEFAULT_CHAT_MEMORY_CONVERSATION_ID, 50),
                QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()), // RAG advisor
                SimpleLoggerAdvisor()
            )
            .build()
    }

    @Bean
    fun simpleVectorStore(embeddingModel: EmbeddingModel): VectorStore {
        return SimpleVectorStore(embeddingModel)
    }

    @Bean
    fun inMemoryChatMemory(): ChatMemory {
        return InMemoryChatMemory()
    }

}