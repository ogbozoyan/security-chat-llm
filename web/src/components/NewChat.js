import React, { useState } from 'react';
import axios from 'axios';

const NewChat = ({ onCreateChat }) => {
    const [chatTitle, setChatTitle] = useState('');

    const handleCreateChat = () => {
        const conversationId = crypto.randomUUID(); // Генерируем новый chatId
        axios.post('http://localhost:8080/api/v1/chat', { title: chatTitle, conversationId })
            .then(response => {
                onCreateChat(response.data); // Отправляем новый чат в родительский компонент
                setChatTitle('');
            })
            .catch(error => {
                console.error("Error creating chat:", error);
            });
    };

    return (
        <div style={{ padding: '20px' }}>
            <h2>Create New Chat</h2>
            <input
                type="text"
                value={chatTitle}
                onChange={(e) => setChatTitle(e.target.value)}
                placeholder="Enter chat title"
                style={{ width: '100%', padding: '10px', marginBottom: '10px' }}
            />
            <button
                onClick={handleCreateChat}
                style={{ width: '100%', padding: '10px' }}
            >
                Create Chat
            </button>
        </div>
    );
};

export default NewChat;
