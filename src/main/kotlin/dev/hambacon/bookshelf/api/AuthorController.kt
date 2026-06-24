package dev.hambacon.bookshelf.api

import jakarta.validation.Valid
import dev.hambacon.bookshelf.application.AuthorService
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
@RequestMapping("/api/authors")
class AuthorController(
    private val authorService: AuthorService,
    private val bookService: BookService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateAuthorRequest): AuthorResponse =
        authorService.create(request)

    @PutMapping("/{authorId}")
    fun update(
        @PathVariable authorId: UUID,
        @Valid @RequestBody request: UpdateAuthorRequest,
    ): AuthorResponse =
        authorService.update(authorId, request)

    @GetMapping("/{authorId}")
    fun get(@PathVariable authorId: UUID): AuthorResponse =
        authorService.get(authorId)

    @GetMapping("/{authorId}/books")
    fun getBooks(@PathVariable authorId: UUID): List<BookResponse> =
        bookService.findByAuthorId(authorId)
}
