package ru.ogbozoyan.core.configuration

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.boot.web.client.ClientHttpRequestFactories
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings
import org.springframework.boot.web.client.RestClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.client.RestClient
import java.time.Duration
import java.util.concurrent.Executor

const val MOCK_USER_ID: String = "MOCK_USER_ID"

@Configuration
class ApplicationConfiguration {

    @Bean(name = ["asyncThreadPoolExecutor"])
    fun taskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 10
        executor.maxPoolSize = 100
        executor.queueCapacity = 500
        executor.setThreadNamePrefix("Async-Coroutine-Thread-")
        executor.initialize()
        return executor
    }

    @Bean
    fun applicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + taskExecutor().asCoroutineDispatcher())

    @Bean
    fun restClientCustomizer(): RestClientCustomizer {
        return RestClientCustomizer { restClientBuilder: RestClient.Builder ->
            restClientBuilder
                .requestFactory(
                    BufferingClientHttpRequestFactory(
                        ClientHttpRequestFactories.get(
                            ClientHttpRequestFactorySettings.DEFAULTS
                                .withConnectTimeout(Duration.ofSeconds(120))
                                .withReadTimeout(Duration.ofSeconds(120))
                        )
                    )
                )
        }
    }
}