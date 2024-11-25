package ru.ogbozoyan.core.configuration

import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.time.LocalDateTime


private const val RESOURCE_NOT_FOUND = "Resource not found"

private const val UNEXPECTED_ERROR = "An unexpected error occurred"

@RestControllerAdvice
class ControllerAdvice(
    private var headers: HttpHeaders = HttpHeaders()
) {

    private val log = LoggerFactory.getLogger(ControllerAdvice::class.java)

    init {
        headers.add(
            "Content-Type",
            "application/json"
        )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentTypeMismatch(
        ex: MethodArgumentTypeMismatchException,
        req: WebRequest
    ): ResponseEntity<CustomErrorMessage> {
        try {
            val errorMessage =
                getErrorMessage(HttpStatus.BAD_REQUEST, ex.message ?: "No message provided", req, ex.javaClass.name)
            return ResponseEntity(errorMessage, headers, HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            return responseIfBrokenControllerAdvice(e, req, ex.javaClass.name)
        }
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMaxUploadSizeExceededException(
        ex: MaxUploadSizeExceededException,
        req: WebRequest
    ): ResponseEntity<CustomErrorMessage> {
        try {
            val errorMessage =
                getErrorMessage(HttpStatus.BAD_REQUEST, ex.message ?: "No message provided", req, ex.javaClass.name)
            return ResponseEntity(errorMessage, headers, HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            return responseIfBrokenControllerAdvice(e, req, ex.javaClass.name)
        }
    }

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationExceptions(
        ex: ConstraintViolationException,
        req: WebRequest
    ): ResponseEntity<CustomErrorMessage> {
        try {
            val errorMessage =
                getErrorMessage(HttpStatus.BAD_REQUEST, ex.message ?: "No message provided", req, ex.javaClass.name)
            return ResponseEntity(errorMessage, headers, HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            return responseIfBrokenControllerAdvice(e, req, ex.javaClass.name)
        }
    }

    @ExceptionHandler(AccessDeniedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleAccessDeniedException(ex: AccessDeniedException, req: WebRequest): ResponseEntity<CustomErrorMessage> {
        try {
            val errorMessage =
                getErrorMessage(HttpStatus.UNAUTHORIZED, ex.message ?: "No message provided", req, ex.javaClass.name)
            return ResponseEntity(errorMessage, headers, HttpStatus.UNAUTHORIZED)
        } catch (e: Exception) {
            return responseIfBrokenControllerAdvice(e, req, ex.javaClass.name)
        }
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNoHandlerFound(ex: NoHandlerFoundException, req: WebRequest): ResponseEntity<CustomErrorMessage> {
        try {
            val errorMessage = getErrorMessage(HttpStatus.NOT_FOUND, RESOURCE_NOT_FOUND, req, ex.javaClass.name)
            return ResponseEntity(errorMessage, headers, HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            return responseIfBrokenControllerAdvice(e, req, ex.javaClass.name)
        }
    }


    @ExceptionHandler(NoResourceFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNoResourceFound(ex: NoResourceFoundException, req: WebRequest): ResponseEntity<CustomErrorMessage> {
        try {
            val errorMessage =
                getErrorMessage(HttpStatus.NOT_FOUND, ex.message ?: "No message provided", req, ex.javaClass.name)
            return ResponseEntity(errorMessage, headers, HttpStatus.NOT_FOUND)
        } catch (e: Exception) {
            return responseIfBrokenControllerAdvice(e, req, ex.javaClass.name)
        }
    }


    @ExceptionHandler(IOException::class, InvocationTargetException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleInternalException(ex: Exception, req: WebRequest): ResponseEntity<CustomErrorMessage> {
        try {
            val errorMessage = getErrorMessage(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.message ?: "No message provided",
                req,
                ex.javaClass.name
            )
            return ResponseEntity(errorMessage, headers, HttpStatus.INTERNAL_SERVER_ERROR)
        } catch (e: Exception) {
            return responseIfBrokenControllerAdvice(e, req, ex.javaClass.name)
        }
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        req: WebRequest
    ): ResponseEntity<CustomErrorMessage> {
        try {
            val errorMessage =
                getErrorMessage(HttpStatus.BAD_REQUEST, ex.message ?: "No message provided", req, ex.javaClass.name)
            return ResponseEntity(errorMessage, headers, HttpStatus.BAD_REQUEST)
        } catch (e: Exception) {
            return responseIfBrokenControllerAdvice(e, req, ex.javaClass.name)
        }
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleGenericException(ex: Exception, req: WebRequest): ResponseEntity<CustomErrorMessage> {
        try {
            val errorMessage =
                getErrorMessage(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    UNEXPECTED_ERROR,
                    req,
                    ex.javaClass.name
                )
            return ResponseEntity(errorMessage, headers, HttpStatus.INTERNAL_SERVER_ERROR)
        } catch (e: Exception) {
            return responseIfBrokenControllerAdvice(e, req, ex.javaClass.name)
        }
    }


    data class CustomErrorMessage(
        val statusCode: Int,
        val timestamp: LocalDateTime,
        val message: String?,
        val description: String?,
        val exceptionName: String?
    )

    private fun getErrorMessage(
        status: HttpStatus,
        message: String?,
        req: WebRequest,
        exceptionName: String
    ): CustomErrorMessage {
        return CustomErrorMessage(
            statusCode = status.value(),
            timestamp = LocalDateTime.now(),
            message = message,
            description = req.getDescription(false),
            exceptionName = exceptionName
        )
    }

    private fun responseIfBrokenControllerAdvice(
        e: Exception,
        req: WebRequest,
        exceptionName: String
    ): ResponseEntity<CustomErrorMessage> {
        log.error("Error during handling {}: {}", exceptionName, e.message)
        return ResponseEntity(
            CustomErrorMessage(
                statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                timestamp = LocalDateTime.now(),
                message = "Internal server error while handling $exceptionName",
                description = req.getDescription(false),
                exceptionName = e.javaClass.name
            ),
            headers,
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}
