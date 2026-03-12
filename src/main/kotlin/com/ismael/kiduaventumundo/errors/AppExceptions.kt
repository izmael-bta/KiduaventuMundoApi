package com.ismael.kiduaventumundo.errors

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val code: String,
    val message: String,
    val details: String? = null
)

open class AppException(
    val status: HttpStatusCode,
    val code: String,
    override val message: String,
    val details: String? = null
) : RuntimeException(message)

class BadRequestException(message: String, details: String? = null) :
    AppException(HttpStatusCode.BadRequest, "BAD_REQUEST", message, details)

class ConflictException(message: String, details: String? = null) :
    AppException(HttpStatusCode.Conflict, "CONFLICT", message, details)

class UnauthorizedException(message: String, details: String? = null) :
    AppException(HttpStatusCode.Unauthorized, "UNAUTHORIZED", message, details)

class NotFoundException(message: String, details: String? = null) :
    AppException(HttpStatusCode.NotFound, "NOT_FOUND", message, details)
