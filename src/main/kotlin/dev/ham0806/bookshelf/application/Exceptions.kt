package dev.ham0806.bookshelf.application

class ResourceNotFoundException(message: String) : RuntimeException(message)

class InvalidRequestException(message: String) : RuntimeException(message)
