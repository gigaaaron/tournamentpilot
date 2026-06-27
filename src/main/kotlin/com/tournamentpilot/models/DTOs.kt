package com.tournamentpilot.models

import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────
//  Domain Data Classes
// ─────────────────────────────────────────────

@Serializable
data class Player(
    val id: Int,
    val name: String,
    val skillLevel: String
)

@Serializable
data class Tournament(
    val id: Int,
    val name: String,
    val status: String
)

@Serializable
data class Match(
    val id: Int,
    val tournamentId: Int,
    val player1Id: Int,
    val player1Name: String,
    val player2Id: Int,
    val player2Name: String,
    val round: Int,
    val winnerId: Int?
)

// ─────────────────────────────────────────────
//  Request Bodies
// ─────────────────────────────────────────────

@Serializable
data class CreatePlayerRequest(
    val name: String,
    val skillLevel: String = "BEGINNER"
)

@Serializable
data class GenerateTournamentRequest(
    val name: String,
    val playerIds: List<Int>  // must be a power of 2: 2, 4, 8, 16 …
)

// ─────────────────────────────────────────────
//  Response Wrappers
// ─────────────────────────────────────────────

@Serializable
data class BracketResponse(
    val tournament: Tournament,
    val rounds: Map<Int, List<Match>>  // round number -> matches in that round
)

@Serializable
data class ApiError(
    val error: String
)
