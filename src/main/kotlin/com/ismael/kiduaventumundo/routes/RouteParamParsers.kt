package com.ismael.kiduaventumundo.routes

import com.ismael.kiduaventumundo.errors.BadRequestException
import io.ktor.server.application.ApplicationCall

internal fun ApplicationCall.longPathParam(name: String): Long {
    return parameters[name]?.toLongOrNull() ?: throw BadRequestException("invalid path parameter: $name")
}

internal fun ApplicationCall.intPathParam(name: String): Int {
    return parameters[name]?.toIntOrNull() ?: throw BadRequestException("invalid path parameter: $name")
}
