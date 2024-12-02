import React, {useState} from 'react';
import ChatList from './components/ChatList';
import ChatWindow from './components/ChatWindow';
import axios from 'axios';

const App = () => {
    const [selectedChatId, setSelectedChatId] = useState(null);
    const [isCreatingChat, setIsCreatingChat] = useState(false);

    const handleSelectChat = (chatId) => {
        setSelectedChatId(chatId);
    };

    const handleCreateChat = () => {
        setIsCreatingChat(true);

        // Отправляем запрос на создание нового чата
        axios.post('http://localhost:8080/api/v1/chat/create', {
            title: "New Chat",
            userId: "mock-user-id",
        })
            .then((response) => {
                const {chatId} = response.data;
                setSelectedChatId(chatId);
            })
            .catch((error) => {
                console.error("Error creating chat:", error);
            })
            .finally(() => {
                setIsCreatingChat(false);
            });
    };

    return (
        <div style={{display: 'flex', height: '100vh', overflow: 'hidden'}}>

            <div style={{display: 'flex', flexDirection: 'column', width: '250px', borderRight: '1px solid #ddd'}}>
                <button
                    onClick={handleCreateChat}
                    style={{
                        padding: '10px',
                        backgroundColor: '#007bff',
                        color: 'white',
                        border: 'none',
                        cursor: isCreatingChat ? 'not-allowed' : 'pointer',
                    }}
                    disabled={isCreatingChat}
                >
                    {isCreatingChat ? 'Creating Chat...' : 'Create Chat'}
                </button>
                <ChatList onSelectChat={handleSelectChat}/>
            </div>

            <div style={{flex: 1}}>
                {selectedChatId ? (
                    <ChatWindow chatId={selectedChatId}/>
                ) : (
                    <div style={{padding: '20px'}}>Please select or create a chat.</div>
                )}
            </div>
        </div>
    );
};

export default App;
