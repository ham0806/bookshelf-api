package dev.hambacon.bookshelf.infrastructure

import dev.hambacon.bookshelf.domain.Author
import dev.hambacon.bookshelf.infrastructure.DbTables.AUTHORS
import dev.hambacon.bookshelf.infrastructure.DbTables.AUTHORS_BIRTH_DATE
import dev.hambacon.bookshelf.infrastructure.DbTables.AUTHORS_CREATED_AT
import dev.hambacon.bookshelf.infrastructure.DbTables.AUTHORS_ID
import dev.hambacon.bookshelf.infrastructure.DbTables.AUTHORS_NAME
import dev.hambacon.bookshelf.infrastructure.DbTables.AUTHORS_UPDATED_AT
import dev.hambacon.bookshelf.infrastructure.DbTables.BOOK_AUTHORS
import dev.hambacon.bookshelf.infrastructure.DbTables.BOOK_AUTHORS_AUTHOR_ID
import dev.hambacon.bookshelf.infrastructure.DbTables.BOOK_AUTHORS_BOOK_ID
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Repository
class JooqAuthorRepository(
    private val dsl: DSLContext,
) : AuthorRepository {
    override fun insert(name: String, birthDate: LocalDate): Author {
        val authorId = UUID.randomUUID()
        val now = OffsetDateTime.now(ZoneOffset.UTC)

        dsl.insertInto(AUTHORS)
            .set(AUTHORS_ID, authorId)
            .set(AUTHORS_NAME, name)
            .set(AUTHORS_BIRTH_DATE, birthDate)
            .set(AUTHORS_CREATED_AT, now)
            .set(AUTHORS_UPDATED_AT, now)
            .execute()

        return findById(authorId) ?: error("登録した著者が取得できません: $authorId")
    }

    override fun update(authorId: UUID, name: String, birthDate: LocalDate) {
        val now = OffsetDateTime.now(ZoneOffset.UTC)

        dsl.update(AUTHORS)
            .set(AUTHORS_NAME, name)
            .set(AUTHORS_BIRTH_DATE, birthDate)
            .set(AUTHORS_UPDATED_AT, now)
            .where(AUTHORS_ID.eq(authorId))
            .execute()
    }

    override fun exists(authorId: UUID): Boolean =
        dsl.fetchExists(
            dsl.selectOne()
                .from(AUTHORS)
                .where(AUTHORS_ID.eq(authorId)),
        )

    override fun findById(authorId: UUID): Author? =
        dsl.select(
            AUTHORS_ID,
            AUTHORS_NAME,
            AUTHORS_BIRTH_DATE,
            AUTHORS_CREATED_AT,
            AUTHORS_UPDATED_AT,
        )
            .from(AUTHORS)
            .where(AUTHORS_ID.eq(authorId))
            .fetchOne { it.toAuthor() }

    override fun findByIds(authorIds: Collection<UUID>): List<Author> {
        if (authorIds.isEmpty()) {
            return emptyList()
        }

        return dsl.select(
            AUTHORS_ID,
            AUTHORS_NAME,
            AUTHORS_BIRTH_DATE,
            AUTHORS_CREATED_AT,
            AUTHORS_UPDATED_AT,
        )
            .from(AUTHORS)
            .where(AUTHORS_ID.`in`(authorIds))
            .orderBy(AUTHORS_NAME.asc(), AUTHORS_ID.asc())
            .fetch { it.toAuthor() }
    }

    override fun findByBookIds(bookIds: Collection<UUID>): Map<UUID, List<Author>> {
        if (bookIds.isEmpty()) {
            return emptyMap()
        }

        return dsl.select(
            BOOK_AUTHORS_BOOK_ID,
            AUTHORS_ID,
            AUTHORS_NAME,
            AUTHORS_BIRTH_DATE,
            AUTHORS_CREATED_AT,
            AUTHORS_UPDATED_AT,
        )
            .from(BOOK_AUTHORS)
            .join(AUTHORS)
            .on(BOOK_AUTHORS_AUTHOR_ID.eq(AUTHORS_ID))
            .where(BOOK_AUTHORS_BOOK_ID.`in`(bookIds))
            .orderBy(BOOK_AUTHORS_BOOK_ID.asc(), AUTHORS_NAME.asc(), AUTHORS_ID.asc())
            .fetch()
            .groupBy { it.get(BOOK_AUTHORS_BOOK_ID) }
            .mapValues { (_, records) -> records.map { it.toAuthor() } }
    }

    private fun Record.toAuthor(): Author =
        Author(
            id = get(AUTHORS_ID),
            name = get(AUTHORS_NAME),
            birthDate = get(AUTHORS_BIRTH_DATE),
            createdAt = get(AUTHORS_CREATED_AT),
            updatedAt = get(AUTHORS_UPDATED_AT),
        )
}

interface AuthorRepository {
    fun insert(name: String, birthDate: LocalDate): Author

    fun update(authorId: UUID, name: String, birthDate: LocalDate)

    fun exists(authorId: UUID): Boolean

    fun findById(authorId: UUID): Author?

    fun findByIds(authorIds: Collection<UUID>): List<Author>

    fun findByBookIds(bookIds: Collection<UUID>): Map<UUID, List<Author>>
}
