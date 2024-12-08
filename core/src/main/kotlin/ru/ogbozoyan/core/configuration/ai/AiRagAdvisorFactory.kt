package ru.ogbozoyan.core.configuration.ai

import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever
import org.springframework.ai.vectorstore.PgVectorStore
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.stereotype.Component
import ru.ogbozoyan.core.configuration.MOCK_CONVERSATION_ID


@Component
class AiRagAdvisorFactory(
    @Value("\${app.advisor.rag.order}") private val RAG_ADVISOR_ORDER: Int,
    @Value("\${app.advisor.rag.enabled}") private val IS_ENABLED: Boolean,
    @Value("\${app.advisor.rag.default-user-text-advice}") private val DEFAULT_USER_TEXT_ADVISE: String,
    @Value("\${app.advisor.rag.default-user-text-advice-ru}") private val DEFAULT_USER_TEXT_ADVISE_RU: String,
    private val vectorStore: PgVectorStore,
) {

    fun retrievalAugmentationAdvisor(): RetrievalAugmentationAdvisor {

        val documentRetriever: VectorStoreDocumentRetriever = VectorStoreDocumentRetriever.builder()
            .vectorStore(vectorStore)
            .similarityThreshold(0.5)
            .topK(3)
            .filterExpression(
                FilterExpressionBuilder()
                    .eq(CHAT_MEMORY_CONVERSATION_ID_KEY, MOCK_CONVERSATION_ID)
                    .build()
            )
            .build()

        val contextualQueryAugmenter = ContextualQueryAugmenter
            .builder()
            .allowEmptyContext(true)
            .build()

        return RetrievalAugmentationAdvisor.builder()
            .documentRetriever(documentRetriever)
            .queryAugmenter(contextualQueryAugmenter)
            .order(HIGHEST_PRECEDENCE + RAG_ADVISOR_ORDER)
            .build()
    }

    fun retrievalAugmentationAdvisor(conversationId: String): RetrievalAugmentationAdvisor {

        val documentRetriever: VectorStoreDocumentRetriever = VectorStoreDocumentRetriever.builder()
            .vectorStore(vectorStore)
            .similarityThreshold(0.5)
            .topK(3)
            .filterExpression(
                FilterExpressionBuilder()
                    .eq(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                    .build()
            )
            .build()

        val contextualQueryAugmenter = ContextualQueryAugmenter
            .builder()
            .allowEmptyContext(true)
            .build()

        return RetrievalAugmentationAdvisor.builder()
            .documentRetriever(documentRetriever)
            .queryAugmenter(contextualQueryAugmenter)
            .order(HIGHEST_PRECEDENCE + RAG_ADVISOR_ORDER)
            .build()
    }

    fun questionAnswerAdvisor(conversationId: String): QuestionAnswerAdvisor =
        QuestionAnswerAdvisor.builder(vectorStore)
            .withSearchRequest(
                SearchRequest.defaults()
                    .withTopK(3)
                    .withFilterExpression(
                        FilterExpressionBuilder()
                            .eq(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                            .build()
                    )
                    .withSimilarityThreshold(0.5)
            )
            .withUserTextAdvise(DEFAULT_USER_TEXT_ADVISE_RU)
            .withOrder(HIGHEST_PRECEDENCE + RAG_ADVISOR_ORDER)
            .build()

    fun questionAnswerAdvisor(): QuestionAnswerAdvisor =
        QuestionAnswerAdvisor.builder(vectorStore)
            .withSearchRequest(
                SearchRequest.defaults()
                    .withTopK(3)
                    .withFilterExpression(
                        FilterExpressionBuilder()
                            .eq(CHAT_MEMORY_CONVERSATION_ID_KEY, MOCK_CONVERSATION_ID)
                            .build()
                    )
                    .withSimilarityThreshold(0.5)
            )
            .withUserTextAdvise(DEFAULT_USER_TEXT_ADVISE)
            .withOrder(HIGHEST_PRECEDENCE + RAG_ADVISOR_ORDER)
            .build()


    fun enabled(): Boolean = IS_ENABLED
}