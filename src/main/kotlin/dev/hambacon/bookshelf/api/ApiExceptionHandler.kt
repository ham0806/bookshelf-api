package dev.hambacon.bookshelf.api

import dev.hambacon.bookshelf.application.InvalidRequestException
import dev.hambacon.bookshelf.application.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(exception: ResourceNotFoundException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.message ?: "リソースが見つかりません").apply {
            title = "Resource not found"
        }

    @ExceptionHandler(InvalidRequestException::class)
    fun handleInvalidRequest(exception: InvalidRequestException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.message ?: "リクエストが不正です").apply {
            title = "Invalid request"
        }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(exception: MethodArgumentNotValidException): ProblemDetail {
        val errors = exception.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }

        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errors).apply {
            title = "Validation failed"
        }
    }
}
