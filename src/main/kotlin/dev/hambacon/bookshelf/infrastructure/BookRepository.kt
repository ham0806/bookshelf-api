package dev.hambacon.bookshelf.infrastructure

import dev.hambacon.bookshelf.domain.Book
import dev.hambacon.bookshelf.domain.PublicationStatus
import dev.hambacon.bookshelf.infrastructure.DbTables.BOOKS
import dev.hambacon.bookshelf.infrastructure.DbTables.BOOKS_CREATED_AT
import dev.hambacon.bookshelf.infrastructure.DbTables.BOOKS_ID
import dev.hambacon.bookshelf.infrastructure.DbTables.BOOKS_PRICE
import dev.hambacon.bookshelf.infrastructure.DbTables.BOOKS_PUBLICATION_STATUS
import dev.hambacon.bookshelf.infrastructure.DbTables.BOOKS_TITLE
import dev.hambacon.bookshelf.infrastructure.DbTables.BOOKS_UPDATED_AT
import dev.hambacon.bookshelf.infrastructure.DbTables.BOOK_AUTHORS
import dev.hambacon.bookshelf.infrastructure.DbTables.BOOK_AUTHORS_AUTHOR_ID
import dev.hambacon.bookshelf.infrastructure.DbTables.BOOK_AUTHORS_BOOK_ID
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
            .set(BOOKS_ID, bookId)
            .set(BOOKS_TITLE, title)
            .set(BOOKS_PRICE, price)
            .set(BOOKS_PUBLICATION_STATUS, publicationStatus.name)
            .set(BOOKS_CREATED_AT, now)
            .set(BOOKS_UPDATED_AT, now)
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
            .set(BOOKS_TITLE, title)
            .set(BOOKS_PRICE, price)
            .set(BOOKS_PUBLICATION_STATUS, publicationStatus.name)
            .set(BOOKS_UPDATED_AT, now)
            .where(BOOKS_ID.eq(bookId))
            .execute()
    }

    override fun findById(bookId: UUID): Book? =
        baseSelect()
            .where(BOOKS_ID.eq(bookId))
            .fetchOne { it.toBook() }

    override fun findByAuthorId(authorId: UUID): List<Book> =
        baseSelect()
            .join(BOOK_AUTHORS)
            .on(BOOKS_ID.eq(BOOK_AUTHORS_BOOK_ID))
            .where(BOOK_AUTHORS_AUTHOR_ID.eq(authorId))
            .orderBy(BOOKS_TITLE.asc(), BOOKS_ID.asc())
            .fetch { it.toBook() }

    override fun replaceAuthors(bookId: UUID, authorIds: List<UUID>) {
        dsl.deleteFrom(BOOK_AUTHORS)
            .where(BOOK_AUTHORS_BOOK_ID.eq(bookId))
            .execute()

        authorIds.forEach { authorId ->
            dsl.insertInto(BOOK_AUTHORS)
                .set(BOOK_AUTHORS_BOOK_ID, bookId)
                .set(BOOK_AUTHORS_AUTHOR_ID, authorId)
                .execute()
        }
    }

    private fun baseSelect() =
        dsl.select(
            BOOKS_ID,
            BOOKS_TITLE,
            BOOKS_PRICE,
            BOOKS_PUBLICATION_STATUS,
            BOOKS_CREATED_AT,
            BOOKS_UPDATED_AT,
        )
            .from(BOOKS)

    private fun Record.toBook(): Book =
        Book(
            id = get(BOOKS_ID),
            title = get(BOOKS_TITLE),
            price = get(BOOKS_PRICE),
            publicationStatus = PublicationStatus.valueOf(get(BOOKS_PUBLICATION_STATUS)),
            createdAt = get(BOOKS_CREATED_AT),
            updatedAt = get(BOOKS_UPDATED_AT),
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
