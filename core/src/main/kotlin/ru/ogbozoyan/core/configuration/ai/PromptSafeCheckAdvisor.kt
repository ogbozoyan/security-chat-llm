package ru.ogbozoyan.core.configuration.ai

import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.api.*
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.core.Ordered
import org.springframework.core.io.Resource
import reactor.core.publisher.Flux


class PromptSafeCheckAdvisor(
    private val promptSafetyClient: ChatClient,
    private val prompt: Resource
) :
    CallAroundAdvisor,
    StreamAroundAdvisor {

    private val log = LoggerFactory.getLogger(PromptSafeCheckAdvisor::class.java)

    private val INJECTION = "YES"

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE
    }

    override fun getName(): String {
        return javaClass.simpleName
    }

    override fun aroundStream(
        advisedRequest: AdvisedRequest,
        chain: StreamAroundAdvisorChain
    ): Flux<AdvisedResponse> {
        try {
            val promptTemplate = PromptTemplate(prompt)
            val promptParameters = HashMap<String, Any>()
            promptParameters["input"] = advisedRequest.userText

            val responseSpec = promptSafetyClient.prompt(promptTemplate.create(promptParameters))
                .call()
            val content = responseSpec.content()

            if (content == null || content.isEmpty() || content.lowercase() == INJECTION) {
                return Flux.just(createFailureResponse(responseSpec.chatResponse(), advisedRequest))
            }
        } catch (e: Throwable) {
            log.error("Error while processing aroundStream in {}", name, e)
        }

        return chain.nextAroundStream(advisedRequest)
    }

    override fun aroundCall(advisedRequest: AdvisedRequest, chain: CallAroundAdvisorChain): AdvisedResponse {
        try {
            val promptTemplate = PromptTemplate(prompt)
            val promptParameters = HashMap<String, Any>()
            promptParameters["input"] = advisedRequest.userText

            val responseSpec = promptSafetyClient.prompt(promptTemplate.create(promptParameters))
                .call()
            val content = responseSpec.content()

            if (content == null || content.isEmpty() || content.uppercase() == INJECTION) {
                return createFailureResponse(responseSpec.chatResponse(), advisedRequest)
            }
        } catch (e: Exception) {
            log.error("Error while processing aroundCall in {}", name, e)
        }

        return chain.nextAroundCall(advisedRequest)
    }

    private fun createFailureResponse(chatResponse: ChatResponse, advisedRequest: AdvisedRequest): AdvisedResponse {
        return AdvisedResponse(chatResponse, advisedRequest.adviseContext())
    }
}