package com.tournamentpilot.routes

import com.tournamentpilot.models.GenerateTournamentRequest
import com.tournamentpilot.service.BracketService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.tournamentRoutes(bracketService: BracketService = BracketService()) {

    route("/tournaments") {

        // ── POST /tournaments/generate ───────────────────────────────────────
        // Generate a single-elimination bracket from a list of player IDs.
        // playerIds.size must be a power of 2 (2, 4, 8, 16 …).
        post("/generate") {
            val request = call.receive<GenerateTournamentRequest>()

            require(request.name.isNotBlank()) { "Tournament name must not be blank." }
            require(request.playerIds.isNotEmpty()) { "playerIds must not be empty." }

            val bracket = bracketService.generateTournament(
                name = request.name,
                playerIds = request.playerIds
            )

            call.respond(HttpStatusCode.Created, bracket)
        }

        // ── GET /tournaments/{id}/matches ────────────────────────────────────
        // Return the current bracket (all rounds + match statuses) for a tournament.
        get("/{id}/matches") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw IllegalArgumentException("id must be a valid integer")

            val bracket = bracketService.getBracket(id)
            call.respond(HttpStatusCode.OK, bracket)
        }
    }
}
