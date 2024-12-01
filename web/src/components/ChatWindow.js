import React, {useEffect, useState} from 'react';
import axios from 'axios';
import {Stomp} from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const ChatWindow = ({chatId}) => {
    const [messages, setMessages] = useState([]);
    const [stompClient, setStompClient] = useState(null);
    const [messageInput, setMessageInput] = useState('');

    useEffect(() => {
        if (!chatId) return;

        // Получаем сообщения чата через REST API
        axios.get(`http://localhost:8080/api/v1/chat/${chatId}/messages/`)
            .then(response => {
                setMessages(response.data);
            })
            .catch(error => {
                console.error("Error fetching messages:", error);
            });

        // Подключаемся к WebSocket, чтобы получать новые сообщения
        const socket = new SockJS('http://localhost:8080/ws');
        const client = Stomp.over(socket);

        client.connect({}, () => {
            console.log('WebSocket connected');
            client.subscribe(`/topic/messages/${chatId}`, (message) => {
                const newMessage = JSON.parse(message.body);
                setMessages((prevMessages) => [...prevMessages, newMessage]);
            });
        });

        setStompClient(client);

        return () => {
            if (stompClient) {
                stompClient.disconnect(() => {
                    console.log('WebSocket disconnected');
                });
            }
        };
    }, [chatId]);

    const handleSendMessage = () => {
        if (messageInput && stompClient) {
            const message = {
                conversationId: chatId,
                question: messageInput
            };
            stompClient.send('/app/chat', {}, JSON.stringify(message));
            setMessageInput('');
        }
    };

    return (
        <div style={{flex: 1, padding: '10px'}}>
            <h2>Chat: {chatId}</h2>
            <div style={{maxHeight: '400px', overflowY: 'auto'}}>
                {messages.map(message => (
                    <div key={message.messageId} style={{padding: '10px', borderBottom: '1px solid #eee'}}>
                        <strong>{message.isUser ? 'You' : 'Bot'}:</strong> {message.content}
                    </div>
                ))}
            </div>
            <input
                type="text"
                value={messageInput}
                onChange={(e) => setMessageInput(e.target.value)}
                placeholder="Type a message..."
                style={{width: '100%', padding: '10px', marginTop: '10px'}}
            />
            <button onClick={handleSendMessage} style={{width: '100%', padding: '10px', marginTop: '10px'}}>
                Send
            </button>
        </div>
    );
};

export default ChatWindow;
