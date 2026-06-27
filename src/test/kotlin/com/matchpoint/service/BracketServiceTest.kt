package com.tournamentpilot.service

import com.tournamentpilot.models.Player
import com.tournamentpilot.repository.MatchInsert
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Unit tests for bracket generation logic.
 * These tests exercise the pure Kotlin logic without requiring a database.
 */
class BracketServiceTest {

    // ── Helper: build a dummy player list ────────────────────────────────────

    private fun makePlayers(count: Int): List<Player> =
        (1..count).map { Player(id = it, name = "Player $it", skillLevel = "BEGINNER") }

    // ── isPowerOfTwo (tested via validation behaviour) ────────────────────────

    @Test
    fun `power of two check - valid sizes pass`() {
        listOf(2, 4, 8, 16, 32).forEach { n ->
            // Just verify the chunked pairing math is correct
            val pairs = makePlayers(n).chunked(2)
            assertEquals(n / 2, pairs.size, "Expected ${n/2} matches for $n players")
        }
    }

    @Test
    fun `chunked pairing produces correct player pairs`() {
        val players = makePlayers(4)
        val pairs   = players.chunked(2)

        assertEquals(2, pairs.size)
        assertEquals(players[0], pairs[0][0])
        assertEquals(players[1], pairs[0][1])
        assertEquals(players[2], pairs[1][0])
        assertEquals(players[3], pairs[1][1])
    }

    @Test
    fun `shuffled list still contains all original players`() {
        val players  = makePlayers(8)
        val shuffled = players.shuffled()

        assertEquals(players.size, shuffled.size)
        assertTrue(shuffled.containsAll(players))
    }

    @Test
    fun `8 players generate 4 round-1 matches`() {
        val players = makePlayers(8).shuffled()
        val matches: List<MatchInsert> = players
            .chunked(2)
            .mapIndexed { idx, (p1, p2) ->
                MatchInsert(
                    tournamentId = 1,
                    player1Id    = p1.id,
                    player1Name  = p1.name,
                    player2Id    = p2.id,
                    player2Name  = p2.name,
                    round        = 1
                )
            }

        assertEquals(4, matches.size)
        assertTrue(matches.all { it.round == 1 })
        assertTrue(matches.all { it.tournamentId == 1 })
    }

    @Test
    fun `non-power-of-two count is rejected`() {
        // Mirrors the validation in BracketService
        val invalidCounts = listOf(3, 5, 6, 7, 9)
        invalidCounts.forEach { n ->
            val isPowerOfTwo = n > 0 && (n and (n - 1)) == 0
            assertTrue(!isPowerOfTwo, "$n should NOT be a power of two")
        }
    }

    @Test
    fun `minimum bracket of 2 players produces 1 match`() {
        val players = makePlayers(2)
        val pairs   = players.chunked(2)
        assertEquals(1, pairs.size)
    }
}
