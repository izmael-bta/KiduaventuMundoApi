package com.ismael.kiduaventumundo

import com.ismael.kiduaventumundo.config.DatabaseSettings
import com.ismael.kiduaventumundo.db.DatabaseFactory
import com.ismael.kiduaventumundo.plugins.configureHTTP
import com.ismael.kiduaventumundo.plugins.configureMonitoring
import com.ismael.kiduaventumundo.plugins.configureSerialization
import com.ismael.kiduaventumundo.plugins.configureStatusPages
import com.ismael.kiduaventumundo.repository.ExposedUserRepository
import com.ismael.kiduaventumundo.routes.registerRoutes
import com.ismael.kiduaventumundo.service.UserService
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module(userServiceOverride: UserService? = null) {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureStatusPages()

    val userService = userServiceOverride ?: run {
        val settings = DatabaseSettings.from(environment.config)
        DatabaseFactory.init(settings)
        UserService(ExposedUserRepository())
    }

    registerRoutes(userService)
}
