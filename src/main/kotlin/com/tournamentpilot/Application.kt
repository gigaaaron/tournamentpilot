package com.tournamentpilot

import com.tournamentpilot.plugins.*
import com.tournamentpilot.routes.playerRoutes
import com.tournamentpilot.routes.tournamentRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.routing.*
import org.slf4j.event.Level

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Serialization (JSON) + Global error handling
    configureSerialization()
    configureStatusPages()

    // Request logging
    install(CallLogging) {
        level = Level.INFO
    }

    // Database (HikariCP + Exposed, auto-creates schema)
    configureDatabase()

    // Swagger UI at /swagger
    configureSwagger()

    // Routes
    routing {
        playerRoutes()
        tournamentRoutes()
    }

    log.info("TournamentPilot is running at http://localhost:8080")
    log.info("Swagger UI: http://localhost:8080/swagger")
}
