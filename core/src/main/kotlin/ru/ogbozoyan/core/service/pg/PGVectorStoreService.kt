package ru.ogbozoyan.core.service.pg

import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.document.Document
import org.springframework.ai.reader.ExtractedTextFormatter
import org.springframework.ai.reader.TextReader
import org.springframework.ai.reader.pdf.PagePdfDocumentReader
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Service
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.CompletableFuture


@Service
@Suppress("LoggingSimilarMessage")
class PGVectorStoreService(
    private val vectorStore: VectorStore,
    private val jdbcClient: JdbcClient,
) {

    private val log: Logger = LoggerFactory.getLogger(PGVectorStoreService::class.java)

    @Value("classpath:/rag/spring-cron-syntax.pdf")
    private val initialPdfResource: Resource? = null

    @Value("classpath:/rag/additional-documentation.txt")
    private val additionalDocumentationTxt: Resource? = null

    @PostConstruct
    fun init() {
        log.info("Checking VectorStore")

        val count = jdbcClient.sql("select count(*) from vector_store")
            .query(Int::class.java)
            .single()

        log.info("Current count of the Vector Store: {}", count)

        if (count == 0) {
            log.info("No VectorStore found, starting initializing vector store.")

            CompletableFuture.supplyAsync {
                saveNewPDFAsync(
                    initialPdfResource!!,
                    initialPdfResource.filename
                )
            }.whenCompleteAsync { _, throwable ->
                if (throwable != null) {
                    log.error("Error occurred while processing PDF: {}", throwable.message)
                } else {
                    log.info("Successfully processed PDF ${initialPdfResource}.")
                }
            }

            CompletableFuture.supplyAsync {
                saveNewTextAsync(
                    additionalDocumentationTxt!!,
                    additionalDocumentationTxt.filename
                )
            }
                .whenCompleteAsync { _, throwable ->
                    if (throwable != null) {
                        log.error("Error occurred while processing text: {}", throwable.message)
                    } else {
                        log.info("Successfully processed text ${additionalDocumentationTxt}.")
                    }
                }
        }
    }

    fun saveNewPDFAsync(pdf: Resource, fName: String?) {
        val fileName = fName ?: "${UUID.randomUUID()}.pdf"
        val textSplitter = TokenTextSplitter()
        try {
            log.info("Loading {} Reference PDF into Vector Store", fileName)
            val config = pdfDocumentReaderConfig(1, 0, 0)

            val pagePdfDocumentReader = PagePdfDocumentReader(pdf, config)
            val documents: List<Document> = pagePdfDocumentReader.get()

            enrichWithFileName(documents, fileName)

            vectorStore.add(textSplitter.apply(documents))
            log.info("Successfully loaded Vector Store by {}", fileName)

        } catch (e: Throwable) {
            log.error(
                "Error while loading PDF {} into Vector Store. Exception: {} - Message: {}",
                fileName,
                e::class.simpleName,
                e.message
            )
            log.debug("Stack trace: ", e)
            throw e
        }
    }

    fun saveNewTextAsync(txt: Resource, fName: String?) {

        val fileName = fName ?: "${UUID.randomUUID()}.txt"
        val textSplitter = TokenTextSplitter()

        try {
            log.info("Loading {} .txt/md files as Documents", fileName)
            val textReader = TextReader(txt)
            textReader.charset = Charset.defaultCharset()
            val documents = textReader.get()

            enrichWithFileName(documents, fileName)

            log.info("Creating and storing Embeddings from Documents")
            vectorStore.accept(textSplitter.split(documents))
            log.info("Successfully loaded Vector Store by {} .txt/md files", fileName)

        } catch (e: Throwable) {
            log.error(
                "Error while loading text {} into Vector Store. Exception: {} - Message: {}",
                fileName,
                e::class.simpleName,
                e.message
            )
            log.debug("Stack trace: ", e)
            throw e

        }
    }

    private fun pdfDocumentReaderConfig(
        pagesPerDocument: Int,
        numberOfBottomTextLinesToDelete: Int,
        numberOfTopTextLinesToDelete: Int
    ): PdfDocumentReaderConfig =
        PdfDocumentReaderConfig.builder()
            .withPageExtractedTextFormatter(
                ExtractedTextFormatter
                    .builder()
                    .withNumberOfBottomTextLinesToDelete(numberOfBottomTextLinesToDelete)
                    .withNumberOfTopPagesToSkipBeforeDelete(numberOfTopTextLinesToDelete)
                    .build()
            )
            .withPagesPerDocument(pagesPerDocument)
            .build()

    /**
     *
     * If processing Resource doesn't have getFileName() at org.springframework.ai.transformer.splitter.TextSplitter#createDocuments will throw NPE
     */
    //TODO: Delete after spring-ai M4 release, i've fixed npe in that milestone)
    private fun enrichWithFileName(
        documents: List<Document>,
        fileName: String
    ): List<Document> {

        for (document: Document in documents) {
            document.metadata["file_name"] = fileName
        }
        return documents
    }
}
