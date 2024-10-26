TODO:
Add elk
Make ollama model file from https://huggingface.co/segolilylabs/Lily-Cybersecurity-7B-v0.2-GGUF
add frontend client like https://openwebui.com/

Implement RAG based on lectures of subject:
1. Find related literature
2. Convert to txt/pdf
3. embed all of them


# HOW TO:
## Ollama: 
```yaml
version: '3.8'
services:
  ollama:
    hostname: ollama
    container_name: ollama
    image: ollama/ollama
    environment:
      - OLLAMA_DEBUG=1
    ports:
      - "11434:11434"
    healthcheck:
      test: ollama --version || exit 1
    entrypoint: [ "ollama run https://huggingface.co/segolilylabs/Lily-Cybersecurity-7B-v0.2-GGUF"]
```

## PG vector store:
```yaml
services:
  pgvector:
    image: 'pgvector/pgvector:pg16'
    environment:
      - 'POSTGRES_DB=core'
      - 'POSTGRES_PASSWORD=admin'
      - 'POSTGRES_USER=admin'
    labels:
      - "org.springframework.boot.service-connection=postgres"
    ports:
      - '5432:5432'
```