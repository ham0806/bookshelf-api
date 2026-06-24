package dev.hambacon.bookshelf.api

import jakarta.validation.Valid
import dev.hambacon.bookshelf.application.BookService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/books")
class BookController(
    private val bookService: BookService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateBookRequest): BookResponse =
        bookService.create(request)

    @PutMapping("/{bookId}")
    fun update(
        @PathVariable bookId: UUID,
        @Valid @RequestBody request: UpdateBookRequest,
    ): BookResponse =
        bookService.update(bookId, request)

    @GetMapping("/{bookId}")
    fun get(@PathVariable bookId: UUID): BookResponse =
        bookService.get(bookId)
}
