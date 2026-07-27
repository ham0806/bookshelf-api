package dev.hambacon.bookshelf.api

import dev.hambacon.bookshelf.application.InvalidRequestException
import dev.hambacon.bookshelf.application.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MissingPathVariableException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(exception: ResourceNotFoundException): ProblemDetail =
        problem(HttpStatus.NOT_FOUND, "Resource not found", exception.message ?: "リソースが見つかりません")

    @ExceptionHandler(InvalidRequestException::class)
    fun handleInvalidRequest(exception: InvalidRequestException): ProblemDetail =
        problem(HttpStatus.BAD_REQUEST, "Invalid request", exception.message ?: "リクエストが不正です")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(exception: MethodArgumentNotValidException): ProblemDetail {
        val errors = exception.bindingResult.fieldErrors.map {
            mapOf(
                "field" to it.field,
                "message" to (it.defaultMessage ?: "値が不正です"),
            )
        }

        return problem(HttpStatus.BAD_REQUEST, "Validation failed", "入力値が不正です").apply {
            setProperty("errors", errors)
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableMessage(): ProblemDetail =
        problem(HttpStatus.BAD_REQUEST, "Malformed request", "JSON リクエストを読み取れません")

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(exception: MethodArgumentTypeMismatchException): ProblemDetail =
        problem(HttpStatus.BAD_REQUEST, "Invalid parameter", "パラメータ '${exception.name}' の形式が不正です")

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(exception: MissingServletRequestParameterException): ProblemDetail =
        problem(HttpStatus.BAD_REQUEST, "Missing parameter", "パラメータ '${exception.parameterName}' が必要です")

    @ExceptionHandler(MissingPathVariableException::class)
    fun handleMissingPathVariable(exception: MissingPathVariableException): ProblemDetail =
        problem(HttpStatus.BAD_REQUEST, "Missing parameter", "パスパラメータ '${exception.variableName}' が必要です")

    private fun problem(status: HttpStatus, title: String, detail: String): ProblemDetail =
        ProblemDetail.forStatusAndDetail(status, detail).apply {
            this.title = title
        }
}
