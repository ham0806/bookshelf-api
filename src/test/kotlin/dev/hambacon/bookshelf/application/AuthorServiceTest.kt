package dev.hambacon.bookshelf.application

import dev.hambacon.bookshelf.api.CreateAuthorRequest
import dev.hambacon.bookshelf.api.UpdateAuthorRequest
import dev.hambacon.bookshelf.domain.Author
import dev.hambacon.bookshelf.infrastructure.AuthorRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class AuthorServiceTest {
    @Test
    fun `著者を登録できる`() {
        val service = AuthorService(
            authorRepository = FakeAuthorRepository(),
        )

        val response = service.create(
            CreateAuthorRequest(
                name = "新規著者",
                birthDate = LocalDate.of(1980, 1, 1),
            ),
        )

        assertEquals("新規著者", response.name)
        assertEquals(LocalDate.of(1980, 1, 1), response.birthDate)
    }

    @Test
    fun `著者を更新できる`() {
        val author = author(name = "更新前")
        val service = AuthorService(
            authorRepository = FakeAuthorRepository(initialAuthors = listOf(author)),
        )

        val response = service.update(
            author.id,
            UpdateAuthorRequest(
                name = "更新後",
                birthDate = LocalDate.of(1980, 1, 2),
            ),
        )

        assertEquals(author.id, response.id)
        assertEquals("更新後", response.name)
        assertEquals(LocalDate.of(1980, 1, 2), response.birthDate)
    }

    @Test
    fun `存在しない著者更新は拒否する`() {
        val authorId = UUID.randomUUID()
        val service = AuthorService(
            authorRepository = FakeAuthorRepository(),
        )

        val exception = assertThrows<ResourceNotFoundException> {
            service.update(
                authorId,
                UpdateAuthorRequest(
                    name = "存在しない著者",
                    birthDate = LocalDate.of(1980, 1, 1),
                ),
            )
        }

        assertEquals("著者が見つかりません: $authorId", exception.message)
    }

    @Test
    fun `存在しない著者取得は拒否する`() {
        val authorId = UUID.randomUUID()
        val service = AuthorService(
            authorRepository = FakeAuthorRepository(),
        )

        val exception = assertThrows<ResourceNotFoundException> {
            service.get(authorId)
        }

        assertEquals("著者が見つかりません: $authorId", exception.message)
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

        private fun now(): OffsetDateTime =
            OffsetDateTime.of(2026, 6, 24, 0, 0, 0, 0, ZoneOffset.UTC)
    }
}
