package ru.ogbozoyan.core.configuration.ai

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor.DEFAULT_RESPONSE_TO_STRING
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor.HIGHEST_PRECEDENCE
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest
import org.springframework.ai.ollama.api.OllamaOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.io.Resource
import org.springframework.jdbc.core.JdbcTemplate
import ru.ogbozoyan.core.PgVectorChatMemory
import ru.ogbozoyan.core.PgVectorChatMemoryConfig
import ru.ogbozoyan.core.configuration.MOCK_CONVERSATION_ID


@Configuration
class AiModelConfiguration(
    private val chatClientBuilder: ChatClient.Builder,
    @Value("classpath:/prompts/system-message-ru.st") private val systemMessageRu: Resource,
    @Value("\${app.advisor.logger.order}") private val LOGGER_ADVISOR_ORDER: Int,
    @Value("\${app.advisor.chat-memory.order}") private val CHAT_MEMORY_ADVISOR_ORDER: Int,
    @Value("\${app.advisor.chat-memory.memory-size}") private val CHAT_MEMORY_SIZE: Int,
    @Value("\${app.advisor.chat-memory.default-system-advise-text}") private val DEFAULT_SYSTEM_TEXT_ADVISE: String,
    @Value("\${app.advisor.chat-memory.default-system-advise-text-ru}") private val DEFAULT_SYSTEM_TEXT_ADVISE_RU: String,
) {

    @Bean
    fun ollamaClient(jdbcTemplate: JdbcTemplate): ChatClient {

        val chatMemoryOrder = HIGHEST_PRECEDENCE + CHAT_MEMORY_ADVISOR_ORDER
        val loggingOrder = Ordered.HIGHEST_PRECEDENCE + LOGGER_ADVISOR_ORDER

        val simpleLoggerAdvisor = SimpleLoggerAdvisor(
            advisedRequestToString(), DEFAULT_RESPONSE_TO_STRING, loggingOrder
        )

        val pgVectorChatMemory = PgVectorChatMemory.create(
            PgVectorChatMemoryConfig.builder()
                .withJdbcTemplate(jdbcTemplate)
                .withInitializeSchema(true)
                .build()
        )


        val chatMemoryAdvisor =
            PromptChatMemoryAdvisor(
                pgVectorChatMemory,
                MOCK_CONVERSATION_ID,
                CHAT_MEMORY_SIZE,
                DEFAULT_SYSTEM_TEXT_ADVISE_RU,
                chatMemoryOrder
            )

        return chatClientBuilder.defaultSystem(systemMessageRu)
            .defaultAdvisors(
                simpleLoggerAdvisor,
                chatMemoryAdvisor,
            )
            .build()
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

