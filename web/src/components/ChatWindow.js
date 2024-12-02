import React, {useEffect, useRef, useState} from 'react';
import axios from 'axios';
import {Stomp} from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const ChatWindow = ({chatId}) => {
    const [messages, setMessages] = useState([]);
    const [stompClient, setStompClient] = useState(null);
    const [messageInput, setMessageInput] = useState('');
    const [isTyping, setIsTyping] = useState(false);
    const messagesEndRef = useRef(null);

    useEffect(() => {
        if (!chatId) return;

        // Загружаем существующие сообщения
        axios
            .get(`http://localhost:8080/api/v1/chat/${chatId}/messages/`)
            .then((response) => {
                setMessages(response.data);
            })
            .catch((error) => {
                console.error('Error fetching messages:', error);
            });

        // Подключаем WebSocket
        const socket = new SockJS('http://localhost:8080/ws');
        const client = Stomp.over(socket);

        client.connect({}, () => {
            console.log('WebSocket connected');
            client.subscribe(`/topic/messages/${chatId}`, (message) => {
                const newMessage = JSON.parse(message.body);

                if (newMessage.content === 'Message saved successfully') {
                    setIsTyping(false);
                    return;
                }

                setMessages((prevMessages) => {
                    const lastMessage = prevMessages[prevMessages.length - 1];

                    if (lastMessage && !lastMessage.isUser && !lastMessage.isFinal) {
                        return prevMessages.map((msg, index) =>
                            index === prevMessages.length - 1
                                ? {...msg, content: msg.content + newMessage.content}
                                : msg
                        );
                    }

                    return [...prevMessages, newMessage];
                });

                setIsTyping(!newMessage.isFinal);
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

    useEffect(() => {
        if (messagesEndRef.current) {
            messagesEndRef.current.scrollIntoView({behavior: 'smooth'});
        }
    }, [messages, isTyping]);

    const handleSendMessage = () => {
        if (messageInput && stompClient) {
            const message = {
                conversationId: chatId,
                question: messageInput,
            };

            setMessages((prevMessages) => [
                ...prevMessages,
                {content: messageInput, isUser: true},
            ]);

            stompClient.send('/app/chat', {}, JSON.stringify(message));
            setMessageInput('');
            setIsTyping(true);
        }
    };

    return (
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', height: '100%' }}>
            <h2>Chat: {chatId}</h2>
            <div style={{ flex: 1, overflowY: 'auto' }}>
                {messages.map((message, index) => (
                    <div
                        key={index}
                        style={{
                            padding: '10px',
                            borderBottom: '1px solid #eee',
                            textAlign: message.isUser ? 'right' : 'left',
                        }}
                    >
                        <strong>{message.isUser ? 'You' : 'Bot'}:</strong> {message.content}
                    </div>
                ))}
                {isTyping && (
                    <div style={{ color: 'gray', fontStyle: 'italic', marginTop: '10px' }}>
                        Generating...
                    </div>
                )}
                <div ref={messagesEndRef} />
            </div>
            {/* Ввод текста и кнопка отправки */}
            <div style={{ display: 'flex', alignItems: 'center', marginTop: '10px', gap: '10px' }}>
                <input
                    type="text"
                    value={messageInput}
                    onChange={(e) => setMessageInput(e.target.value)}
                    placeholder="Type a message..."
                    style={{
                        flex: 1,
                        padding: '10px',
                    }}
                    disabled={isTyping}
                />
                <button
                    onClick={handleSendMessage}
                    style={{
                        padding: '10px 20px',
                        background: isTyping ? '#ccc' : 'yellow',
                        cursor: isTyping ? 'not-allowed' : 'pointer',
                        border: 'none',
                    }}
                    disabled={isTyping}
                >
                    Send
                </button>
            </div>
        </div>
    );

};

export default ChatWindow;