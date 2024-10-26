#!/bin/sh

if ollama list | grep -q 'Lily-Cybersecurity'; then
  echo "Lily-Cybersecurity-7B-v0.2-GGUF is already installed."
else
  echo "Lily-Cybersecurity-7B not found. Pulling the model..."
  ollama pull https://huggingface.co/segolilylabs/Lily-Cybersecurity-7B-v0.2-GGUF
fi

if ollama list | grep -q 'mxbai-embed-large'; then
  echo "mxbai-embed-large: is already installed."
else
  echo "mxbai-embed-large: not found. Pulling the model..."
  ollama pull mxbai-embed-large:
fi
