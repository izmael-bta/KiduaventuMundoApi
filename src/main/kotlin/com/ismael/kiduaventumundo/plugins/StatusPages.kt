package com.ismael.kiduaventumundo.plugins

import com.ismael.kiduaventumundo.errors.AppException
import com.ismael.kiduaventumundo.errors.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<AppException> { call, cause ->
            call.respond(
                cause.status,
                ErrorResponse(code = cause.code, message = cause.message, details = cause.details)
            )
        }

        exception<BadRequestException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(code = "BAD_REQUEST", message = "Invalid request", details = cause.message)
            )
        }

        exception<ContentTransformationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(code = "BAD_REQUEST", message = "Invalid request payload", details = cause.message)
            )
        }

        exception<Throwable> { call, _ ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(code = "INTERNAL_ERROR", message = "Unexpected server error")
            )
        }
    }
}
