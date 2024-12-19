#!/bin/bash

# Start Ollama in the background.
/bin/ollama serve &
# Record Process ID.
pid=$!

# Pause for Ollama to start.
sleep 10

echo "🔴 Retrieve embedding model"
ollama pull nomic-embed-text:latest
echo "🟢 Done!"

# Wait for Ollama process to finish.
wait $pid &

pid=$!

echo "🔴 Retrieve llm model"
ollama pull llama3.1:latest
echo "🟢 Done!"

# Wait for Ollama process to finish.
wait $pid