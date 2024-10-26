package ru.ogbozoyan.core.configuration

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
        executor.setThreadNamePrefix("Async-")
        executor.initialize()
        return executor
    }
}