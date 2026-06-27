package com.tournamentpilot.repository

import com.tournamentpilot.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class TournamentRepository {

    // ── Tournament CRUD ──────────────────────────────────────────────────────

    fun createTournament(name: String, status: String = "IN_PROGRESS"): Tournament = transaction {
        val id = Tournaments.insert {
            it[Tournaments.name] = name
            it[Tournaments.status] = status
        } get Tournaments.id

        Tournament(id = id, name = name, status = status)
    }

    fun findTournamentById(tournamentId: Int): Tournament = transaction {
        Tournaments.select { Tournaments.id eq tournamentId }
            .map { Tournament(it[Tournaments.id], it[Tournaments.name], it[Tournaments.status]) }
            .firstOrNull()
            ?: throw NoSuchElementException("Tournament $tournamentId not found")
    }

    // ── Match CRUD ───────────────────────────────────────────────────────────

    /**
     * Bulk-insert all Round 1 matches for a tournament.
     * Returns the persisted Match records (with generated IDs).
     */
    fun insertMatches(matchInserts: List<MatchInsert>): List<Match> = transaction {
        matchInserts.map { m ->
            val id = Matches.insert {
                it[Matches.tournamentId] = m.tournamentId
                it[Matches.player1Id]   = m.player1Id
                it[Matches.player2Id]   = m.player2Id
                it[Matches.round]       = m.round
                it[Matches.winnerId]    = null
            } get Matches.id

            Match(
                id           = id,
                tournamentId = m.tournamentId,
                player1Id    = m.player1Id,
                player1Name  = m.player1Name,
                player2Id    = m.player2Id,
                player2Name  = m.player2Name,
                round        = m.round,
                winnerId     = null
            )
        }
    }

    /**
     * Fetch all matches for a tournament, enriched with player names,
     * grouped by round.
     */
    fun findMatchesByTournament(tournamentId: Int): Map<Int, List<Match>> = transaction {
        // Alias the Players table twice to join player1 and player2 names
        val p1 = Players.alias("p1")
        val p2 = Players.alias("p2")

        Matches
            .join(p1, JoinType.INNER, Matches.player1Id, p1[Players.id])
            .join(p2, JoinType.INNER, Matches.player2Id, p2[Players.id])
            .select { Matches.tournamentId eq tournamentId }
            .orderBy(Matches.round to SortOrder.ASC, Matches.id to SortOrder.ASC)
            .map { row ->
                Match(
                    id           = row[Matches.id],
                    tournamentId = row[Matches.tournamentId],
                    player1Id    = row[Matches.player1Id],
                    player1Name  = row[p1[Players.name]],
                    player2Id    = row[Matches.player2Id],
                    player2Name  = row[p2[Players.name]],
                    round        = row[Matches.round],
                    winnerId     = row[Matches.winnerId]
                )
            }
            .groupBy { it.round } // Map<round, List<Match>>
    }
}

/** Lightweight value object used only during bracket generation. */
data class MatchInsert(
    val tournamentId: Int,
    val player1Id: Int,
    val player1Name: String,
    val player2Id: Int,
    val player2Name: String,
    val round: Int
)
