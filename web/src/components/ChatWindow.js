import React, {useEffect, useRef, useState} from 'react';
import axios from 'axios';
import {Stomp} from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const ChatWindow = ({chatId}) => {
    const [messages, setMessages] = useState([]);
    const [stompClient, setStompClient] = useState(null);
    const [messageInput, setMessageInput] = useState('');
    const [isTyping, setIsTyping] = useState(false);
    const [selectedFile, setSelectedFile] = useState(null);
    const [fileType, setFileType] = useState('');
    const messagesEndRef = useRef(null);
    const receivedParts = useRef(new Set()); // Track received parts to prevent duplicates

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
                    receivedParts.current.clear();
                    setIsTyping(false);
                    return;
                }

                if (receivedParts.current.has(newMessage.partOrder)) {
                    return;
                }
                receivedParts.current.add(newMessage.partOrder);


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

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        setSelectedFile(file);

        if (file) {
            const extension = file.name.split('.').pop().toLowerCase();
            let type;

            switch (extension) {
                case 'pdf':
                    type = 'PDF';
                    break;
                case 'txt':
                    type = 'TXT';
                    break;
                case 'md':
                    type = 'MD';
                    break;
                default:
                    type = 'TXT';
            }

            setFileType(type);
        }
    };

    const handleFileUpload = async () => {
        if (!selectedFile) return;

        setIsTyping(true);

        const formData = new FormData();
        formData.append('file', selectedFile);
        formData.append('type', fileType);
        formData.append('chatId', chatId);

        try {
            await axios.post('http://localhost:8080/api/v1/embed-file', formData, {
                headers: {'Content-Type': 'multipart/form-data'},
            });

            setMessages((prevMessages) => [
                ...prevMessages,
                {content: selectedFile.name, isUser: true, type: fileType},
            ]);
        } catch (error) {
            console.error('Error uploading file:', error);
        } finally {
            setSelectedFile(null);
            setIsTyping(false);
            setTimeout(() => {
                setIsTyping(false);
            }, 1000); // Разблокируем ввод через 2 секунды
        }
    };

    return (
        <div style={{flex: 1, display: 'flex', flexDirection: 'column', height: '100%'}}>
            <h2>Chat: {chatId}</h2>
            <div style={{flex: 1, overflowY: 'auto'}}>
                {messages.map((message, index) => (
                    <div
                        key={index}
                        style={{
                            padding: '10px',
                            borderBottom: '1px solid #eee',
                            textAlign: message.isUser ? 'right' : 'left',
                        }}
                    >
                        <strong>{message.isUser ? 'You' : 'Bot'}:</strong>{' '}
                        {message.type === 'PDF' || message.type === 'PLAIN' ? (
                            <span>
                                <i className="fa fa-file" aria-hidden="true"></i> {message.content}
                            </span>
                        ) : (
                            message.content
                        )}
                    </div>
                ))}
                {isTyping && (
                    <div style={{color: 'gray', fontStyle: 'italic', marginTop: '10px'}}>
                        Generating...
                    </div>
                )}
                <div ref={messagesEndRef}/>
            </div>
            <div>
                <div style={{display: 'flex', alignItems: 'center', marginBottom: '10px'}}>
                    <input
                        type="file"
                        onChange={handleFileChange}
                        disabled={isTyping}
                        style={{flex: 1, marginRight: '10px'}}
                    />
                    <button
                        onClick={handleFileUpload}
                        style={{
                            padding: '10px',
                            background: selectedFile ? 'yellow' : '#ccc',
                            cursor: selectedFile ? 'pointer' : 'not-allowed',
                        }}
                        disabled={!selectedFile || isTyping}
                    >
                        Upload
                    </button>
                </div>
                <input
                    type="text"
                    value={messageInput}
                    onChange={(e) => setMessageInput(e.target.value)}
                    placeholder="Type a message..."
                    disabled={isTyping}
                    style={{width: '100%', padding: '10px', marginTop: '10px'}}
                />
                {!isTyping && (
                    <button
                        onClick={handleSendMessage}
                        style={{
                            width: '100%',
                            padding: '10px',
                            marginTop: '10px',
                            background: 'yellow',
                        }}
                    >
                        Send
                    </button>
                )}
            </div>
        </div>
    );
};

export default ChatWindow;