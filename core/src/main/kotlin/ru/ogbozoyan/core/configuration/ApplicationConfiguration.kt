package ru.ogbozoyan.core.configuration

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor


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

}