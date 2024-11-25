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
        registry.enableSimpleBroker("/topic") // Префикс для сообщений от сервера
        registry.setApplicationDestinationPrefixes("/app") // Префикс для сообщений к серверу
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/chat-websocket") // Точка входа WebSocket
            .setAllowedOrigins("*") // Укажите домены клиента
            .withSockJS() // Опционально, для поддержки SockJS
    }
}