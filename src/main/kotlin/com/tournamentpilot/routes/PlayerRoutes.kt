package com.tournamentpilot.routes

import com.tournamentpilot.models.CreatePlayerRequest
import com.tournamentpilot.repository.PlayerRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.playerRoutes(playerRepo: PlayerRepository = PlayerRepository()) {

    route("/players") {

        // ── POST /players ────────────────────────────────────────────────────
        // Register a new player.
        post {
            val request = call.receive<CreatePlayerRequest>()

            require(request.name.isNotBlank()) { "Player name must not be blank." }
            require(request.skillLevel in VALID_SKILL_LEVELS) {
                "skillLevel must be one of: $VALID_SKILL_LEVELS"
            }

            val player = playerRepo.create(request)
            call.respond(HttpStatusCode.Created, player)
        }

        // ── GET /players ─────────────────────────────────────────────────────
        // List all registered players.
        get {
            val players = playerRepo.findAll()
            call.respond(HttpStatusCode.OK, players)
        }

        // ── GET /players/{id} ────────────────────────────────────────────────
        // Fetch one player by ID.
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw IllegalArgumentException("id must be a valid integer")

            val player = playerRepo.findById(id)   // throws NoSuchElementException if missing
            call.respond(HttpStatusCode.OK, player)
        }
    }
}

private val VALID_SKILL_LEVELS = setOf("BEGINNER", "INTERMEDIATE", "ADVANCED", "A", "B", "C", "D")
