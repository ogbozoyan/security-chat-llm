package ru.ogbozoyan.core.configuration

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.DEFAULT_CHAT_MEMORY_CONVERSATION_ID
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor.DEFAULT_RESPONSE_TO_STRING
import org.springframework.ai.chat.client.advisor.VectorStoreChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.ollama.api.OllamaOptions
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever
import org.springframework.ai.vectorstore.SimpleVectorStore
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.io.Resource


@Configuration
class AiModelConfiguration(
    private val chatClientBuilder: ChatClient.Builder,
    private val vectorStore: VectorStore,
    @Value("classpath:/prompts/system-message-ru.st") private val systemMessage: Resource,
) {

    private val CHAT_MEMORY_SIZE = 10

    @Bean
    fun ollamaClient(): ChatClient {

        val chatMemoryAdvisor =
            VectorStoreChatMemoryAdvisor.builder(vectorStore).withOrder(Ordered.LOWEST_PRECEDENCE - 500)
                .withChatMemoryRetrieveSize(CHAT_MEMORY_SIZE).withConversationId(DEFAULT_CHAT_MEMORY_CONVERSATION_ID)
                .build()

        val documentRetriever: VectorStoreDocumentRetriever = VectorStoreDocumentRetriever.builder()
            .vectorStore(vectorStore).similarityThreshold(0.5).topK(5)
            .build()

        val queryAugmenter = ContextualQueryAugmenter.builder().allowEmptyContext(true).build()

        val retrievalAugmentationAdvisor =
            RetrievalAugmentationAdvisor.builder().documentRetriever(documentRetriever).queryAugmenter(queryAugmenter)
                .order(Ordered.LOWEST_PRECEDENCE - 1_000).build()

        val simpleLoggerAdvisor = SimpleLoggerAdvisor(
            advisedRequestToString(), DEFAULT_RESPONSE_TO_STRING, Ordered.HIGHEST_PRECEDENCE
        )

        return chatClientBuilder.defaultSystem(systemMessage)
            .defaultAdvisors(chatMemoryAdvisor, simpleLoggerAdvisor, retrievalAugmentationAdvisor)
            .build()
    }

    @Bean
    fun simpleVectorStore(embeddingModel: EmbeddingModel): VectorStore {
        return SimpleVectorStore(embeddingModel)
    }

    private fun advisedRequestToString() = { req: AdvisedRequest ->

        val chatOptions = req.chatOptions

        buildString {
            append("User Text: ${req.userText.replace("\n", " \\n ").ifBlank { "N/A" }}; ")
            append("System Text: ${req.systemText?.replace("\n", " \\n ")?.ifBlank { "N/A" }}; ")

            if (chatOptions is OllamaOptions) {
                append("Chat Options:")
                append("  Use NUMA: ${chatOptions.useNUMA ?: "N/A"} ")
                append("  Context Size (num_ctx): ${chatOptions.numCtx ?: "N/A"} ")
                append("  Batch Size (num_batch): ${chatOptions.numBatch ?: "N/A"} ")
                append("  GPU Layers (num_gpu): ${chatOptions.numGPU ?: "N/A"} ")
                append("  Main GPU: ${chatOptions.mainGPU ?: "N/A"} ")
                append("  Low VRAM: ${chatOptions.lowVRAM ?: "N/A"} ")
                append("  F16 KV: ${chatOptions.f16KV ?: "N/A"} ")
                append("  Num Threads: ${chatOptions.numThread ?: "N/A"} ")
                append("  Temperature: ${chatOptions.temperature ?: "N/A"} ")
                append("  Top-K Sampling: ${chatOptions.topK ?: "N/A"} ")
                append("  Top-P Sampling: ${chatOptions.topP ?: "N/A"} ")
                append("  Repeat Penalty: ${chatOptions.repeatPenalty ?: "N/A"}; ")
            } else {
                append("Chat Options: ${chatOptions ?: "N/A"}; ")
            }
            append("Media: ${req.media.ifEmpty { "N/A" }}; ")
            append("Function Names: ${req.functionNames.ifEmpty { "N/A" }}; ")
            append("Function Callbacks: ${req.functionCallbacks.ifEmpty { "N/A" }}; ")
            append("Messages: ${req.messages.ifEmpty { "N/A" }}; ")
            append("User Params: ${req.userParams.ifEmpty { "N/A" }}; ")
            append("System Params: ${req.systemParams.ifEmpty { "N/A" }}; ")
            append("Advisors:")
            if (req.advisors.isNotEmpty()) {
                req.advisors.forEach { advisor ->
                    append(" ${advisor.name}; ")
                }
            } else {
                append("None; ")
            }
            append("Advisor Params: ${req.advisorParams.ifEmpty { "N/A" }}; ")
            append(
                "Advise Context: ${
                    req.adviseContext.map { (key, value) ->
                        val sanitizedKey = key.replace("\n", "\\n")
                        val sanitizedValue = value?.toString()?.replace("\n", "\\n") ?: "N/A"
                        "$sanitizedKey=$sanitizedValue"
                    }.joinToString(", ", "{", "}")
                }; "
            )
            append("Tool Context: ${req.toolContext.ifEmpty { "N/A" }}; ")
        }
    }
}

