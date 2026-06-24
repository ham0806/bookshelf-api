package dev.ham0806.bookshelf.domain

import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class Author(
    val id: UUID,
    val name: String,
    val birthDate: LocalDate,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
)
