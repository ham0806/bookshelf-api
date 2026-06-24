package dev.hambacon.bookshelf.application

import dev.hambacon.bookshelf.api.AuthorSummaryResponse
import dev.hambacon.bookshelf.api.BookResponse
import dev.hambacon.bookshelf.api.CreateBookRequest
import dev.hambacon.bookshelf.api.UpdateBookRequest
import dev.hambacon.bookshelf.domain.Author
import dev.hambacon.bookshelf.domain.Book
import dev.hambacon.bookshelf.domain.PublicationStatus
import dev.hambacon.bookshelf.infrastructure.AuthorRepository
import dev.hambacon.bookshelf.infrastructure.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BookService(
    private val bookRepository: BookRepository,
    private val authorRepository: AuthorRepository,
) {
    @Transactional
    fun create(request: CreateBookRequest): BookResponse {
        val authorIds = request.authorIds.distinct()
        ensureAuthorsExist(authorIds)

        val book = bookRepository.insert(
            title = request.title,
            price = request.price,
            publicationStatus = request.publicationStatus,
        )
        bookRepository.replaceAuthors(book.id, authorIds)

        return get(book.id)
    }

    @Transactional
    fun update(bookId: UUID, request: UpdateBookRequest): BookResponse {
        val current = bookRepository.findById(bookId)
            ?: throw ResourceNotFoundException("書籍が見つかりません: $bookId")
        val authorIds = request.authorIds.distinct()
        ensureAuthorsExist(authorIds)

        if (current.publicationStatus == PublicationStatus.PUBLISHED &&
            request.publicationStatus == PublicationStatus.UNPUBLISHED
        ) {
            throw InvalidRequestException("出版済みの書籍は未出版へ変更できません")
        }

        bookRepository.update(
            bookId = bookId,
            title = request.title,
            price = request.price,
            publicationStatus = request.publicationStatus,
        )
        bookRepository.replaceAuthors(bookId, authorIds)

        return get(bookId)
    }

    @Transactional(readOnly = true)
    fun get(bookId: UUID): BookResponse {
        val book = bookRepository.findById(bookId)
            ?: throw ResourceNotFoundException("書籍が見つかりません: $bookId")
        val authors = authorRepository.findByBookIds(listOf(bookId))[bookId].orEmpty()

        return book.toResponse(authors)
    }

    @Transactional(readOnly = true)
    fun findByAuthorId(authorId: UUID): List<BookResponse> {
        if (!authorRepository.exists(authorId)) {
            throw ResourceNotFoundException("著者が見つかりません: $authorId")
        }

        val books = bookRepository.findByAuthorId(authorId)
        val authorsByBookId = authorRepository.findByBookIds(books.map { it.id })

        return books.map { book ->
            book.toResponse(authorsByBookId[book.id].orEmpty())
        }
    }

    private fun ensureAuthorsExist(authorIds: List<UUID>) {
        if (authorIds.isEmpty()) {
            throw InvalidRequestException("書籍には1人以上の著者が必要です")
        }

        val foundAuthorIds = authorRepository.findByIds(authorIds).map { it.id }.toSet()
        val missingAuthorIds = authorIds.filterNot { it in foundAuthorIds }
        if (missingAuthorIds.isNotEmpty()) {
            throw ResourceNotFoundException("著者が見つかりません: ${missingAuthorIds.joinToString(", ")}")
        }
    }

    private fun Book.toResponse(authors: List<Author>): BookResponse =
        BookResponse(
            id = id,
            title = title,
            price = price,
            publicationStatus = publicationStatus,
            authors = authors.map { it.toSummaryResponse() },
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    private fun Author.toSummaryResponse(): AuthorSummaryResponse =
        AuthorSummaryResponse(
            id = id,
            name = name,
            birthDate = birthDate,
        )
}
