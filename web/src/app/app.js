// Подключаемся к API через Server-Sent Events (SSE)
const eventSourceUrl = 'http://localhost:8080/api/v1/chat'; 
const outputDiv = document.getElementById('output');

let collectedContent = ''; // Хранилище для полной строки текста

function appendContent(content) {
    collectedContent += content; // Добавляем новый фрагмент
    outputDiv.textContent = collectedContent; // Обновляем вывод
}

function startSSE() {
    const eventSource = new EventSource(eventSourceUrl);

    // Событие получения данных
    eventSource.onmessage = (event) => {
        try {
            const data = JSON.parse(event.data);
            if (data.content !== undefined) {
                appendContent(data.content);
            }
        } catch (err) {
            console.error('Ошибка обработки данных:', err);
        }
    };

    // Событие при ошибке
    eventSource.onerror = (error) => {
        console.error('Ошибка SSE соединения:', error);
        eventSource.close();
        outputDiv.textContent += '\n\n[Соединение закрыто]';
    };
}

// Запускаем соединение
startSSE();
