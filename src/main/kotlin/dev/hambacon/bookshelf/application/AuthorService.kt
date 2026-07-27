package dev.hambacon.bookshelf.application

import dev.hambacon.bookshelf.api.AuthorResponse
import dev.hambacon.bookshelf.api.CreateAuthorRequest
import dev.hambacon.bookshelf.api.UpdateAuthorRequest
import dev.hambacon.bookshelf.domain.Author
import dev.hambacon.bookshelf.infrastructure.AuthorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AuthorService(
    private val authorRepository: AuthorRepository,
) {
    @Transactional
    fun create(request: CreateAuthorRequest): AuthorResponse =
        authorRepository.insert(request.name, request.birthDate).toResponse()

    @Transactional
    fun update(authorId: UUID, request: UpdateAuthorRequest): AuthorResponse {
        ensureExists(authorId)
        authorRepository.update(authorId, request.name, request.birthDate)
        return get(authorId)
    }

    @Transactional(readOnly = true)
    fun get(authorId: UUID): AuthorResponse =
        findOrThrow(authorId).toResponse()

    @Transactional(readOnly = true)
    private fun findOrThrow(authorId: UUID): Author =
        authorRepository.findById(authorId)
            ?: throw ResourceNotFoundException("著者が見つかりません: $authorId")

    private fun ensureExists(authorId: UUID) {
        if (!authorRepository.exists(authorId)) {
            throw ResourceNotFoundException("著者が見つかりません: $authorId")
        }
    }

    private fun Author.toResponse(): AuthorResponse =
        AuthorResponse(
            id = id,
            name = name,
            birthDate = birthDate,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
