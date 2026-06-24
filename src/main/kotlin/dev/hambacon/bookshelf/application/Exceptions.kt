package dev.hambacon.bookshelf.application

class ResourceNotFoundException(message: String) : RuntimeException(message)

class InvalidRequestException(message: String) : RuntimeException(message)
