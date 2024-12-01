package ru.ogbozoyan.core.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        // Настраиваем брокер сообщений
        registry.enableSimpleBroker("/topic") // Клиенты слушают этот префикс
        registry.setApplicationDestinationPrefixes("/app") // Префикс для сообщений, отправляемых на сервер
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // Настраиваем конечную точку WebSocket
        registry.addEndpoint("/ws") // URL для подключения WebSocket
            .setAllowedOriginPatterns("*") // Разрешаем все источники
            .withSockJS() // Включаем SockJS для совместимости
    }
}