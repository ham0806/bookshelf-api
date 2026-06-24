package dev.hambacon.bookshelf.domain

import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

data class Book(
    val id: UUID,
    val title: String,
    val price: BigDecimal,
    val publicationStatus: PublicationStatus,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
