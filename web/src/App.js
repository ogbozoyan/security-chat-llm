import React, { useState } from 'react';
import ChatList from './components/ChatList';
import ChatWindow from './components/ChatWindow';

const App = () => {
    const [selectedChatId, setSelectedChatId] = useState(null);

    const handleSelectChat = (chatId) => {
        setSelectedChatId(chatId);
    };

    return (
        <div style={{ display: 'flex' }}>

            {/* Панель с чатами слева */}
            <ChatList onSelectChat={handleSelectChat} />
            {/* Окно чата с сообщениями справа */}
            <ChatWindow chatId={selectedChatId} />
        </div>
    );
};

export default App;