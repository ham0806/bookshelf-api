package dev.hambacon.bookshelf.api

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Size
import dev.hambacon.bookshelf.domain.PublicationStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class CreateAuthorRequest(
    @field:NotBlank
    @field:Size(max = 200)
    val name: String,

    @field:PastOrPresent
    val birthDate: LocalDate,
)

data class UpdateAuthorRequest(
    @field:NotBlank
    @field:Size(max = 200)
    val name: String,

    @field:PastOrPresent
    val birthDate: LocalDate,
)

data class CreateBookRequest(
    @field:NotBlank
    @field:Size(max = 500)
    val title: String,

    @field:DecimalMin(value = "0.00", inclusive = true)
    val price: BigDecimal,

    @field:Size(min = 1)
    val authorIds: List<UUID>,

    val publicationStatus: PublicationStatus = PublicationStatus.UNPUBLISHED,
)

data class UpdateBookRequest(
    @field:NotBlank
    @field:Size(max = 500)
    val title: String,

    @field:DecimalMin(value = "0.00", inclusive = true)
    val price: BigDecimal,

    @field:Size(min = 1)
    val authorIds: List<UUID>,

    val publicationStatus: PublicationStatus,
)
