# Security Chat

Spring AI based web application whose purpose is to demonstrate how to easily build RAG applications on Spring stack.

All models runs locally which is requires [OLLAMA](https://github.com/ollama/ollama).  
[Frontend](web)
[Backend](core/src)

### What is RAG ?

Retrieval-augmented generation (RAG) is a technique for enhancing the accuracy and reliability of generative AI models
with facts fetched from external sources.

### How to implement RAG ?

Implement RAG based on lectures of subject:

1. Find related literature
2. Convert to txt/pdf
3. embed all of them

# Build

## MacOS

Unfortunately OLLAMA docker image requires NVIDIA GPU drivers [see issue](https://github.com/ollama/ollama/issues/3417).
The only way to run it locally, is [manual build](README.md#manually). 

## Linux


### Docker compose

1.

 ```shell
 docker compose up --build
 ```

2.

 ```shell
 docker compose up -d
 ```

### Manually

1. Install [Ollama](https://ollama.com/download)
2. Pull models

```shell
ollama pull nomic-embed-text:latest #you can choose your own model
```

```shell
ollama pull llama3.1:latest #you can choose your own model
```

3.

```shell

ollama serve

```

4. Run Postgres using docker

```shell
docker run -d \
  --name pgvector-container \
  -e POSTGRES_DB=core \
  -e POSTGRES_PASSWORD=admin \
  -e POSTGRES_USER=admin \
  -p 5432:5432 \
  -v pgvector-data:/var/lib/postgresql/data \
  --label "org.springframework.boot.service-connection=postgres" \
  pgvector/pgvector:pg16
```

5. Build and Run Web

```shell
cd web
npm i
npm start
```

6. Build and Run Backend

```shell
cd core
gradle bootRun
```
