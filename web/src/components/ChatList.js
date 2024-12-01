import React, { useState, useEffect } from 'react';
import axios from 'axios';

const ChatList = ({ onSelectChat }) => {
    const [chats, setChats] = useState([]);

    useEffect(() => {
        // Получаем список всех чатов
        axios.get('http://localhost:8080/api/v1/chat/mock-user')
            .then(response => {
                setChats(response.data);
            })
            .catch(error => {
                console.error("Error fetching chats:", error);
            });
    }, []);

    return (
        <div style={{ width: '250px', padding: '10px', borderRight: '1px solid #ddd', height: '100vh', overflowY: 'auto' }}>
            <h2>Chats</h2>
            <ul style={{ listStyleType: 'none', paddingLeft: '0' }}>
                {chats.map(chat => (
                    <li key={chat.chatId} style={{ padding: '10px', borderBottom: '1px solid #eee', cursor: 'pointer' }} onClick={() => onSelectChat(chat.chatId)}>
                        {chat.title}
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default ChatList;
