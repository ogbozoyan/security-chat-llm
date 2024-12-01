import React, {useEffect, useState} from 'react';
import axios from 'axios';
import {Stomp} from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const ChatWindow = ({chatId}) => {
    const [messages, setMessages] = useState([]);
    const [stompClient, setStompClient] = useState(null);
    const [messageInput, setMessageInput] = useState('');
    const [isTyping, setIsTyping] = useState(false);

    useEffect(() => {
        if (!chatId) return;

        // Fetch existing messages for the chat
        axios
            .get(`http://localhost:8080/api/v1/chat/${chatId}/messages/`)
            .then((response) => {
                setMessages(response.data);
            })
            .catch((error) => {
                console.error('Error fetching messages:', error);
            });

        // Connect to WebSocket
        const socket = new SockJS('http://localhost:8080/ws');
        const client = Stomp.over(socket);

        client.connect({}, () => {
            console.log('WebSocket connected');
            client.subscribe(`/topic/messages/${chatId}`, (message) => {
                const newMessage = JSON.parse(message.body);

                // Ignore system messages like "Message saved successfully"
                if (newMessage.content === 'Message saved successfully') {
                    setIsTyping(false)
                    return;
                }

                setMessages((prevMessages) => {
                    const lastMessage = prevMessages[prevMessages.length - 1];

                    // Check if the last message is a bot message and incomplete
                    if (lastMessage && !lastMessage.isUser && !lastMessage.isFinal) {
                        // Append content to the last bot message
                        return prevMessages.map((msg, index) =>
                            index === prevMessages.length - 1
                                ? {...msg, content: msg.content + newMessage.content}
                                : msg
                        );
                    }

                    // Otherwise, add the new message as a separate entry
                    return [...prevMessages, newMessage];
                });

                // Update typing status based on whether the bot's message is complete
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

    const handleSendMessage = () => {
        if (messageInput && stompClient) {
            const message = {
                conversationId: chatId,
                question: messageInput,
            };

            // Add the user's message to the chat
            setMessages((prevMessages) => [
                ...prevMessages,
                {content: messageInput, isUser: true},
            ]);

            // Send the message via WebSocket
            stompClient.send('/app/chat', {}, JSON.stringify(message));
            setMessageInput('');
            setIsTyping(true); // Bot starts typing
        }
    };

    return (
        <div style={{flex: 1, padding: '10px'}}>
            <h2>Chat: {chatId}</h2>
            <div style={{maxHeight: '400px', overflowY: 'auto'}}>
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
                {/* Typing animation */}
                {isTyping && (
                    <div style={{color: 'gray', fontStyle: 'italic', marginTop: '10px'}}>
                        Generating...
                    </div>
                )}
            </div>
            <input
                type="text"
                value={messageInput}
                onChange={(e) => setMessageInput(e.target.value)}
                placeholder="Type a message..."
                style={{width: '100%', padding: '10px', marginTop: '10px'}}
            />
            {!isTyping && (
                <button onClick={handleSendMessage} style={{width: '100%', padding: '10px', marginTop: '10px'}}>
                    Send
                </button>
            )}
        </div>
    );
};

export default ChatWindow;
