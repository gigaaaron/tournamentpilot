package com.tournamentpilot.repository

import com.tournamentpilot.models.CreatePlayerRequest
import com.tournamentpilot.models.Player
import com.tournamentpilot.models.Players
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class PlayerRepository {

    /** Insert a new player and return the full record. */
    fun create(request: CreatePlayerRequest): Player = transaction {
        val id = Players.insert {
            it[name]       = request.name.trim()
            it[skillLevel] = request.skillLevel.uppercase()
        } get Players.id

        Player(id = id, name = request.name.trim(), skillLevel = request.skillLevel.uppercase())
    }

    /** Fetch one player by PK — throws NoSuchElementException if missing. */
    fun findById(playerId: Int): Player = transaction {
        Players.select { Players.id eq playerId }
            .map(::rowToPlayer)
            .firstOrNull()
            ?: throw NoSuchElementException("Player $playerId not found")
    }

    /** Fetch multiple players by a list of PKs (preserves order of input ids). */
    fun findByIds(playerIds: List<Int>): List<Player> = transaction {
        Players.select { Players.id inList playerIds }
            .map(::rowToPlayer)
            .sortedBy { playerIds.indexOf(it.id) }   // keep caller's ordering
    }

    /** Return all players ordered by name. */
    fun findAll(): List<Player> = transaction {
        Players.selectAll()
            .orderBy(Players.name to SortOrder.ASC)
            .map(::rowToPlayer)
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private fun rowToPlayer(row: ResultRow) = Player(
        id = row[Players.id],
        name  = row[Players.name],
        skillLevel = row[Players.skillLevel]
    )
}
