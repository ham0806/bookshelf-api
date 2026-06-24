package dev.hambacon.bookshelf.api

import dev.hambacon.bookshelf.domain.PublicationStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class AuthorResponse(
    val id: UUID,
    val name: String,
    val birthDate: LocalDate,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)

data class AuthorSummaryResponse(
    val id: UUID,
    val name: String,
    val birthDate: LocalDate,
)

data class BookResponse(
    val id: UUID,
    val title: String,
    val price: BigDecimal,
    val publicationStatus: PublicationStatus,
    val authors: List<AuthorSummaryResponse>,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
