import { Client } from '@stomp/stompjs';

// Настройка STOMP клиента
const stompClient = new Client({
    brokerURL: 'ws://localhost:8080/chat-websocket',
    connectHeaders: {
        login: 'guest', // Если авторизация не настроена, можно оставить пусто
        passcode: 'guest',
    },
    debug: (str) => {
        console.log(str);
    },
    onConnect: () => {
        console.log('Connected to WebSocket');

        // Подписка на сообщения
        stompClient.subscribe('/topic/messages', (message) => {
            console.log('Received message:', JSON.parse(message.body));
        });

        // Отправка сообщения
        stompClient.publish({
            destination: '/app/chat',
            body: JSON.stringify({
                conversationId: '12345',
                question: 'Hello, how are you?',
            }),
        });
    },
    onStompError: (frame) => {
        console.error('Broker reported error:', frame.headers['message']);
        console.error('Additional details:', frame.body);
    },
});

// Запуск подключения
stompClient.activate();
