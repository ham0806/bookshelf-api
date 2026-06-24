package dev.hambacon.bookshelf.application

import dev.hambacon.bookshelf.api.CreateBookRequest
import dev.hambacon.bookshelf.api.UpdateBookRequest
import dev.hambacon.bookshelf.domain.Author
import dev.hambacon.bookshelf.domain.Book
import dev.hambacon.bookshelf.domain.PublicationStatus
import dev.hambacon.bookshelf.infrastructure.AuthorRepository
import dev.hambacon.bookshelf.infrastructure.BookRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class BookServiceTest {
    @Test
    fun `出版済み書籍を未出版へ戻す更新は拒否する`() {
        val author = author(name = "既存著者")
        val book = book(publicationStatus = PublicationStatus.PUBLISHED)
        val bookRepository = FakeBookRepository(initialBooks = listOf(book))
        val service = BookService(
            bookRepository = bookRepository,
            authorRepository = FakeAuthorRepository(initialAuthors = listOf(author)),
        )

        val exception = assertThrows<InvalidRequestException> {
            service.update(
                book.id,
                UpdateBookRequest(
                    title = "更新後タイトル",
                    price = BigDecimal("1200.00"),
                    authorIds = listOf(author.id),
                    publicationStatus = PublicationStatus.UNPUBLISHED,
                ),
            )
        }

        assertEquals("出版済みの書籍は未出版へ変更できません", exception.message)
        assertEquals(PublicationStatus.PUBLISHED, bookRepository.findById(book.id)?.publicationStatus)
        assertNull(bookRepository.replacedAuthors)
    }

    @Test
    fun `著者が0人の書籍登録は拒否する`() {
        val service = BookService(
            bookRepository = FakeBookRepository(),
            authorRepository = FakeAuthorRepository(),
        )

        val exception = assertThrows<InvalidRequestException> {
            service.create(
                CreateBookRequest(
                    title = "著者なしの本",
                    price = BigDecimal("1000.00"),
                    authorIds = emptyList(),
                    publicationStatus = PublicationStatus.UNPUBLISHED,
                ),
            )
        }

        assertEquals("書籍には1人以上の著者が必要です", exception.message)
    }

    @Test
    fun `存在しない著者を指定した書籍登録は拒否する`() {
        val missingAuthorId = UUID.randomUUID()
        val service = BookService(
            bookRepository = FakeBookRepository(),
            authorRepository = FakeAuthorRepository(),
        )

        val exception = assertThrows<ResourceNotFoundException> {
            service.create(
                CreateBookRequest(
                    title = "存在しない著者の本",
                    price = BigDecimal("1000.00"),
                    authorIds = listOf(missingAuthorId),
                    publicationStatus = PublicationStatus.UNPUBLISHED,
                ),
            )
        }

        assertEquals("著者が見つかりません: $missingAuthorId", exception.message)
    }

    private class FakeAuthorRepository(
        initialAuthors: List<Author> = emptyList(),
    ) : AuthorRepository {
        private val authors = initialAuthors.associateBy { it.id }.toMutableMap()

        override fun insert(name: String, birthDate: LocalDate): Author {
            val author = author(name = name, birthDate = birthDate)
            authors[author.id] = author
            return author
        }

        override fun update(authorId: UUID, name: String, birthDate: LocalDate) {
            val current = authors.getValue(authorId)
            authors[authorId] = current.copy(name = name, birthDate = birthDate, updatedAt = now())
        }

        override fun exists(authorId: UUID): Boolean =
            authors.containsKey(authorId)

        override fun findById(authorId: UUID): Author? =
            authors[authorId]

        override fun findByIds(authorIds: Collection<UUID>): List<Author> =
            authorIds.mapNotNull { authors[it] }

        override fun findByBookIds(bookIds: Collection<UUID>): Map<UUID, List<Author>> =
            emptyMap()
    }

    private class FakeBookRepository(
        initialBooks: List<Book> = emptyList(),
    ) : BookRepository {
        private val books = initialBooks.associateBy { it.id }.toMutableMap()
        var replacedAuthors: Pair<UUID, List<UUID>>? = null
            private set

        override fun insert(
            title: String,
            price: BigDecimal,
            publicationStatus: PublicationStatus,
        ): Book {
            val book = book(title = title, price = price, publicationStatus = publicationStatus)
            books[book.id] = book
            return book
        }

        override fun update(
            bookId: UUID,
            title: String,
            price: BigDecimal,
            publicationStatus: PublicationStatus,
        ) {
            val current = books.getValue(bookId)
            books[bookId] = current.copy(
                title = title,
                price = price,
                publicationStatus = publicationStatus,
                updatedAt = now(),
            )
        }

        override fun findById(bookId: UUID): Book? =
            books[bookId]

        override fun findByAuthorId(authorId: UUID): List<Book> =
            books.values.toList()

        override fun replaceAuthors(bookId: UUID, authorIds: List<UUID>) {
            replacedAuthors = bookId to authorIds
        }
    }

    companion object {
        private fun author(
            id: UUID = UUID.randomUUID(),
            name: String = "著者",
            birthDate: LocalDate = LocalDate.of(1980, 1, 1),
        ): Author =
            Author(
                id = id,
                name = name,
                birthDate = birthDate,
                createdAt = now(),
                updatedAt = now(),
            )

        private fun book(
            id: UUID = UUID.randomUUID(),
            title: String = "書籍",
            price: BigDecimal = BigDecimal("1000.00"),
            publicationStatus: PublicationStatus = PublicationStatus.UNPUBLISHED,
        ): Book =
            Book(
                id = id,
                title = title,
                price = price,
                publicationStatus = publicationStatus,
                createdAt = now(),
                updatedAt = now(),
            )

        private fun now(): OffsetDateTime =
            OffsetDateTime.of(2026, 6, 24, 0, 0, 0, 0, ZoneOffset.UTC)
    }
}
