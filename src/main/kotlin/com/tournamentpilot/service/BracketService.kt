package com.tournamentpilot.service

import com.tournamentpilot.models.BracketResponse
import com.tournamentpilot.models.Player
import com.tournamentpilot.repository.MatchInsert
import com.tournamentpilot.repository.PlayerRepository
import com.tournamentpilot.repository.TournamentRepository

/**
 * BracketService
 *
 * Owns all bracket-generation logic. Deliberately kept free of HTTP concerns —
 * it talks only in domain objects and throws on invalid input.
 */
class BracketService(
    private val playerRepo: PlayerRepository      = PlayerRepository(),
    private val tournamentRepo: TournamentRepository = TournamentRepository()
) {

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Generate a single-elimination tournament for [playerIds].
     *
     * Rules enforced:
     *  - At least 2 players required.
     *  - Player count must be a power of 2 (2, 4, 8, 16…).
     *  - All player IDs must exist in the database.
     *
     * The players are randomly shuffled before pairing, so seeding is fair.
     */
    fun generateTournament(name: String, playerIds: List<Int>): BracketResponse {
        validatePlayerCount(playerIds)

        // Resolve player records (throws NoSuchElementException on unknown IDs)
        val players = playerRepo.findByIds(playerIds)
        validateAllPlayersFound(playerIds, players)

        // Create the tournament row
        val tournament = tournamentRepo.createTournament(name)

        // Build Round 1 match inserts from a shuffled player list
        val round1Matches = buildRound1Matches(
            tournamentId = tournament.id,
            players      = players.shuffled()      // ← Kotlin stdlib: random order
        )

        // Persist all matches and retrieve them grouped by round
        tournamentRepo.insertMatches(round1Matches)
        val rounds = tournamentRepo.findMatchesByTournament(tournament.id)

        return BracketResponse(tournament = tournament, rounds = rounds)
    }

    /**
     * Fetch the current bracket state (all rounds and match results).
     */
    fun getBracket(tournamentId: Int): BracketResponse {
        val tournament = tournamentRepo.findTournamentById(tournamentId)
        val rounds     = tournamentRepo.findMatchesByTournament(tournamentId)
        return BracketResponse(tournament = tournament, rounds = rounds)
    }

    // ── Bracket Logic ────────────────────────────────────────────────────────

    /**
     * Pair up a shuffled player list into Round 1 matches.
     *
     * .chunked(2) splits [P1, P2, P3, P4, …] into [[P1,P2], [P3,P4], …]
     * Each chunk becomes one match.
     */
    private fun buildRound1Matches(
        tournamentId: Int,
        players: List<Player>              // already shuffled by caller
    ): List<MatchInsert> =
        players
            .chunked(2)                    // ← Kotlin stdlib: split into pairs
            .map { (p1, p2) ->             // destructure each 2-element chunk
                MatchInsert(
                    tournamentId = tournamentId,
                    player1Id    = p1.id,
                    player1Name  = p1.name,
                    player2Id    = p2.id,
                    player2Name  = p2.name,
                    round        = 1
                )
            }

    // ── Validation ───────────────────────────────────────────────────────────

    private fun validatePlayerCount(playerIds: List<Int>) {
        val n = playerIds.size
        require(n >= 2) { "At least 2 players are required to generate a bracket." }
        require(isPowerOfTwo(n)) {
            "Player count must be a power of 2 (e.g. 2, 4, 8, 16). Received: $n"
        }
    }

    private fun validateAllPlayersFound(requestedIds: List<Int>, found: List<Player>) {
        val foundIds    = found.map { it.id }.toSet()
        val missingIds  = requestedIds.filterNot { it in foundIds }
        require(missingIds.isEmpty()) { "Unknown player IDs: $missingIds" }
    }

    /**
     * Bitwise power-of-two check: a positive integer n is a power of 2
     * if and only if n AND (n-1) == 0.
     *
     * Example:  8 = 1000b,  7 = 0111b  →  1000 AND 0111 = 0000 ✓
     */
    private fun isPowerOfTwo(n: Int): Boolean = n > 0 && (n and (n - 1)) == 0
}
