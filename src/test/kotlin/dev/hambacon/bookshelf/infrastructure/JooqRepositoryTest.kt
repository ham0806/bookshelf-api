package dev.hambacon.bookshelf.infrastructure

import dev.hambacon.bookshelf.domain.PublicationStatus
import dev.hambacon.bookshelf.PostgresIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@Sql(
    statements = [
        "DELETE FROM book_authors",
        "DELETE FROM books",
        "DELETE FROM authors",
    ],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
)
class JooqRepositoryTest(
    @Autowired private val authorRepository: AuthorRepository,
    @Autowired private val bookRepository: BookRepository,
) : PostgresIntegrationTest() {
    @Test
    fun `著者を登録して更新できる`() {
        val author = authorRepository.insert(
            name = "夏目 漱石",
            birthDate = LocalDate.of(1867, 2, 9),
        )

        authorRepository.update(
            authorId = author.id,
            name = "夏目 漱石 更新",
            birthDate = LocalDate.of(1867, 2, 10),
        )

        val found = authorRepository.findById(author.id)

        assertTrue(authorRepository.exists(author.id))
        assertFalse(authorRepository.exists(UUID.randomUUID()))
        assertEquals(author.id, found?.id)
        assertEquals("夏目 漱石 更新", found?.name)
        assertEquals(LocalDate.of(1867, 2, 10), found?.birthDate)
        assertNotNull(found?.updatedAt)
    }

    @Test
    fun `書籍の著者を差し替えて著者別に取得できる`() {
        val firstAuthor = authorRepository.insert("著者 A", LocalDate.of(1980, 1, 1))
        val secondAuthor = authorRepository.insert("著者 B", LocalDate.of(1981, 1, 1))
        val book = bookRepository.insert(
            title = "初期タイトル",
            price = BigDecimal("1000.00"),
            publicationStatus = PublicationStatus.UNPUBLISHED,
        )

        bookRepository.replaceAuthors(book.id, listOf(firstAuthor.id))
        bookRepository.replaceAuthors(book.id, listOf(secondAuthor.id))

        val booksByFirstAuthor = bookRepository.findByAuthorId(firstAuthor.id)
        val booksBySecondAuthor = bookRepository.findByAuthorId(secondAuthor.id)
        val authorsByBookId = authorRepository.findByBookIds(listOf(book.id))

        assertEquals(emptyList<UUID>(), booksByFirstAuthor.map { it.id })
        assertEquals(listOf(book.id), booksBySecondAuthor.map { it.id })
        assertEquals(listOf(secondAuthor.id), authorsByBookId.getValue(book.id).map { it.id })
    }

    @Test
    fun `書籍を更新できる`() {
        val book = bookRepository.insert(
            title = "更新前タイトル",
            price = BigDecimal("1000.00"),
            publicationStatus = PublicationStatus.UNPUBLISHED,
        )

        bookRepository.update(
            bookId = book.id,
            title = "更新後タイトル",
            price = BigDecimal("1200.00"),
            publicationStatus = PublicationStatus.PUBLISHED,
        )

        val found = bookRepository.findById(book.id)

        assertEquals(book.id, found?.id)
        assertEquals("更新後タイトル", found?.title)
        assertEquals(BigDecimal("1200.00"), found?.price)
        assertEquals(PublicationStatus.PUBLISHED, found?.publicationStatus)
        assertNotNull(found?.updatedAt)
    }
}
