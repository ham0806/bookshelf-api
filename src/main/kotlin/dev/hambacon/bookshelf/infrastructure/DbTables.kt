package dev.hambacon.bookshelf.infrastructure

import org.jooq.Field
import org.jooq.Record
import org.jooq.Table
import org.jooq.impl.DSL
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

object DbTables {
    val AUTHORS: Table<Record> = DSL.table(DSL.name("authors"))
    val AUTHORS_ID: Field<UUID> = DSL.field(DSL.name("authors", "id"), UUID::class.java)
    val AUTHORS_NAME: Field<String> = DSL.field(DSL.name("authors", "name"), String::class.java)
    val AUTHORS_BIRTH_DATE: Field<LocalDate> = DSL.field(DSL.name("authors", "birth_date"), LocalDate::class.java)
    val AUTHORS_CREATED_AT: Field<OffsetDateTime> =
        DSL.field(DSL.name("authors", "created_at"), OffsetDateTime::class.java)
    val AUTHORS_UPDATED_AT: Field<OffsetDateTime> =
        DSL.field(DSL.name("authors", "updated_at"), OffsetDateTime::class.java)

    val BOOKS: Table<Record> = DSL.table(DSL.name("books"))
    val BOOKS_ID: Field<UUID> = DSL.field(DSL.name("books", "id"), UUID::class.java)
    val BOOKS_TITLE: Field<String> = DSL.field(DSL.name("books", "title"), String::class.java)
    val BOOKS_PRICE: Field<BigDecimal> = DSL.field(DSL.name("books", "price"), BigDecimal::class.java)
    val BOOKS_PUBLICATION_STATUS: Field<String> =
        DSL.field(DSL.name("books", "publication_status"), String::class.java)
    val BOOKS_CREATED_AT: Field<OffsetDateTime> =
        DSL.field(DSL.name("books", "created_at"), OffsetDateTime::class.java)
    val BOOKS_UPDATED_AT: Field<OffsetDateTime> =
        DSL.field(DSL.name("books", "updated_at"), OffsetDateTime::class.java)

    val BOOK_AUTHORS: Table<Record> = DSL.table(DSL.name("book_authors"))
    val BOOK_AUTHORS_BOOK_ID: Field<UUID> = DSL.field(DSL.name("book_authors", "book_id"), UUID::class.java)
    val BOOK_AUTHORS_AUTHOR_ID: Field<UUID> = DSL.field(DSL.name("book_authors", "author_id"), UUID::class.java)
}
