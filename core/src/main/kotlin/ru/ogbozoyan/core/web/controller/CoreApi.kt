package ru.ogbozoyan.core.web.controller

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.multipart.MultipartFile
import ru.ogbozoyan.core.service.ingestion.FileTypeEnum
import ru.ogbozoyan.core.web.dto.ApiRequest
import ru.ogbozoyan.core.web.dto.ApiResponse

interface CoreApi {
    @PostMapping(
        "/api/v1/query",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(summary = "Ask question to llm and save chat", description = "Returns the result")
    @ResponseStatus(HttpStatus.OK)
    fun query(@RequestBody request: ApiRequest): ResponseEntity<ApiResponse>

    @PostMapping(
        "/api/v1/embed-file",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Ask question to llm and save chat", description = "Returns the result")
    fun embedFile(file: MultipartFile, type: FileTypeEnum)
}