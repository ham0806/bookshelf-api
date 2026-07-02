package dev.hambacon.bookshelf.infrastructure

import dev.hambacon.bookshelf.domain.Author
import dev.hambacon.bookshelf.jooq.Tables.AUTHORS
import dev.hambacon.bookshelf.jooq.Tables.BOOK_AUTHORS
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
            .set(AUTHORS.ID, authorId)
            .set(AUTHORS.NAME, name)
            .set(AUTHORS.BIRTH_DATE, birthDate)
            .set(AUTHORS.CREATED_AT, now)
            .set(AUTHORS.UPDATED_AT, now)
            .execute()

        return findById(authorId) ?: error("登録した著者が取得できません: $authorId")
    }

    override fun update(authorId: UUID, name: String, birthDate: LocalDate) {
        val now = OffsetDateTime.now(ZoneOffset.UTC)

        dsl.update(AUTHORS)
            .set(AUTHORS.NAME, name)
            .set(AUTHORS.BIRTH_DATE, birthDate)
            .set(AUTHORS.UPDATED_AT, now)
            .where(AUTHORS.ID.eq(authorId))
            .execute()
    }

    override fun exists(authorId: UUID): Boolean =
        dsl.fetchExists(
            dsl.selectOne()
                .from(AUTHORS)
                .where(AUTHORS.ID.eq(authorId)),
        )

    override fun findById(authorId: UUID): Author? =
        dsl.select(
            AUTHORS.ID,
            AUTHORS.NAME,
            AUTHORS.BIRTH_DATE,
            AUTHORS.CREATED_AT,
            AUTHORS.UPDATED_AT,
        )
            .from(AUTHORS)
            .where(AUTHORS.ID.eq(authorId))
            .fetchOne { it.toAuthor() }

    override fun findByIds(authorIds: Collection<UUID>): List<Author> {
        if (authorIds.isEmpty()) {
            return emptyList()
        }

        return dsl.select(
            AUTHORS.ID,
            AUTHORS.NAME,
            AUTHORS.BIRTH_DATE,
            AUTHORS.CREATED_AT,
            AUTHORS.UPDATED_AT,
        )
            .from(AUTHORS)
            .where(AUTHORS.ID.`in`(authorIds))
            .orderBy(AUTHORS.NAME.asc(), AUTHORS.ID.asc())
            .fetch { it.toAuthor() }
    }

    override fun findByBookIds(bookIds: Collection<UUID>): Map<UUID, List<Author>> {
        if (bookIds.isEmpty()) {
            return emptyMap()
        }

        return dsl.select(
            BOOK_AUTHORS.BOOK_ID,
            AUTHORS.ID,
            AUTHORS.NAME,
            AUTHORS.BIRTH_DATE,
            AUTHORS.CREATED_AT,
            AUTHORS.UPDATED_AT,
        )
            .from(BOOK_AUTHORS)
            .join(AUTHORS)
            .on(BOOK_AUTHORS.AUTHOR_ID.eq(AUTHORS.ID))
            .where(BOOK_AUTHORS.BOOK_ID.`in`(bookIds))
            .orderBy(BOOK_AUTHORS.BOOK_ID.asc(), AUTHORS.NAME.asc(), AUTHORS.ID.asc())
            .fetch()
            .groupBy { it.get(BOOK_AUTHORS.BOOK_ID) }
            .mapValues { (_, records) -> records.map { it.toAuthor() } }
    }

    private fun Record.toAuthor(): Author =
        Author(
            id = get(AUTHORS.ID),
            name = get(AUTHORS.NAME),
            birthDate = get(AUTHORS.BIRTH_DATE),
            createdAt = get(AUTHORS.CREATED_AT),
            updatedAt = get(AUTHORS.UPDATED_AT),
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
