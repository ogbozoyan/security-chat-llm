package ru.ogbozoyan.core.configuration

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.DEFAULT_CHAT_MEMORY_CONVERSATION_ID
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.client.advisor.VectorStoreChatMemoryAdvisor
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever
import org.springframework.ai.vectorstore.SimpleVectorStore
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered


@Configuration
class AiModelConfiguration(
    private val chatClientBuilder: ChatClient.Builder,
    private val vectorStore: VectorStore
) {

    @Bean
    fun ollamaClient(): ChatClient {
        val documentRetriever: VectorStoreDocumentRetriever = VectorStoreDocumentRetriever.builder()
            .vectorStore(vectorStore)
            .similarityThreshold(0.50)
            .topK(3)
            .build()


        return chatClientBuilder
            .defaultAdvisors(
                VectorStoreChatMemoryAdvisor(vectorStore, DEFAULT_CHAT_MEMORY_CONVERSATION_ID, 5),
                SimpleLoggerAdvisor(),
                RetrievalAugmentationAdvisor.builder()
                    .documentRetriever(documentRetriever)
                    .order(Ordered.HIGHEST_PRECEDENCE)
                    .queryAugmenter(
                        ContextualQueryAugmenter
                            .builder()
                            .allowEmptyContext(true)
                            .build()
                    )
                    .build()
            )
            .build()
    }

    @Bean
    fun simpleVectorStore(embeddingModel: EmbeddingModel): VectorStore {
        return SimpleVectorStore(embeddingModel)
    }

}