package com.tournamentpilot.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun Application.configureSwagger() {
    routing {
        // Swagger UI available at: http://localhost:8080/swagger
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
    }
}
