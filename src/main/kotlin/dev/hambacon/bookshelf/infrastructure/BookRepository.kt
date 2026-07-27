package dev.hambacon.bookshelf.infrastructure

import dev.hambacon.bookshelf.domain.Book
import dev.hambacon.bookshelf.domain.PublicationStatus
import dev.hambacon.bookshelf.jooq.Tables.BOOKS
import dev.hambacon.bookshelf.jooq.Tables.BOOK_AUTHORS
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Repository
class JooqBookRepository(
    private val dsl: DSLContext,
) : BookRepository {
    override fun insert(
        title: String,
        price: BigDecimal,
        publicationStatus: PublicationStatus,
    ): Book {
        val bookId = UUID.randomUUID()
        val now = OffsetDateTime.now(ZoneOffset.UTC)

        dsl.insertInto(BOOKS)
            .set(BOOKS.ID, bookId)
            .set(BOOKS.TITLE, title)
            .set(BOOKS.PRICE, price)
            .set(BOOKS.PUBLICATION_STATUS, publicationStatus.name)
            .set(BOOKS.CREATED_AT, now)
            .set(BOOKS.UPDATED_AT, now)
            .execute()

        return findById(bookId) ?: error("登録した書籍が取得できません: $bookId")
    }

    override fun update(
        bookId: UUID,
        title: String,
        price: BigDecimal,
        publicationStatus: PublicationStatus,
    ) {
        val now = OffsetDateTime.now(ZoneOffset.UTC)

        dsl.update(BOOKS)
            .set(BOOKS.TITLE, title)
            .set(BOOKS.PRICE, price)
            .set(BOOKS.PUBLICATION_STATUS, publicationStatus.name)
            .set(BOOKS.UPDATED_AT, now)
            .where(BOOKS.ID.eq(bookId))
            .execute()
    }

    override fun findById(bookId: UUID): Book? =
        baseSelect()
            .where(BOOKS.ID.eq(bookId))
            .fetchOne { it.toBook() }

    override fun findByAuthorId(authorId: UUID): List<Book> =
        baseSelect()
            .join(BOOK_AUTHORS)
            .on(BOOKS.ID.eq(BOOK_AUTHORS.BOOK_ID))
            .where(BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
            .orderBy(BOOKS.TITLE.asc(), BOOKS.ID.asc())
            .fetch { it.toBook() }

    override fun replaceAuthors(bookId: UUID, authorIds: List<UUID>) {
        dsl.deleteFrom(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
            .execute()

        authorIds.forEach { authorId ->
            dsl.insertInto(BOOK_AUTHORS)
                .set(BOOK_AUTHORS.BOOK_ID, bookId)
                .set(BOOK_AUTHORS.AUTHOR_ID, authorId)
                .execute()
        }
    }

    private fun baseSelect() =
        dsl.select(
            BOOKS.ID,
            BOOKS.TITLE,
            BOOKS.PRICE,
            BOOKS.PUBLICATION_STATUS,
            BOOKS.CREATED_AT,
            BOOKS.UPDATED_AT,
        )
            .from(BOOKS)

    private fun Record.toBook(): Book =
        Book(
            id = get(BOOKS.ID),
            title = get(BOOKS.TITLE),
            price = get(BOOKS.PRICE),
            publicationStatus = PublicationStatus.valueOf(get(BOOKS.PUBLICATION_STATUS)),
            createdAt = get(BOOKS.CREATED_AT),
            updatedAt = get(BOOKS.UPDATED_AT),
        )
}

interface BookRepository {
    fun insert(
        title: String,
        price: BigDecimal,
        publicationStatus: PublicationStatus,
    ): Book

    fun update(
        bookId: UUID,
        title: String,
        price: BigDecimal,
        publicationStatus: PublicationStatus,
    )

    fun findById(bookId: UUID): Book?

    fun findByAuthorId(authorId: UUID): List<Book>

    fun replaceAuthors(bookId: UUID, authorIds: List<UUID>)
}
