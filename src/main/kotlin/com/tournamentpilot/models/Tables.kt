package com.tournamentpilot.models

import org.jetbrains.exposed.sql.Table

// ─────────────────────────────────────────────
//  Database Table Definitions (Exposed DSL)
// ─────────────────────────────────────────────

object Players : Table("players") {
    val id = integer("id").autoIncrement()
    val name  = varchar("name", 100)
    val skillLevel = varchar("skill_level", 20) // e.g. BEGINNER, INTERMEDIATE, ADVANCED, A, B, C, D

    override val primaryKey = PrimaryKey(id)
}

object Tournaments : Table("tournaments") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val status = varchar("status", 20) // PENDING, IN_PROGRESS, COMPLETED

    override val primaryKey = PrimaryKey(id)
}

object Matches : Table("matches") {
    val id = integer("id").autoIncrement()
    val tournamentId = integer("tournament_id").references(Tournaments.id)
    val player1Id = integer("player1_id").references(Players.id)
    val player2Id = integer("player2_id").references(Players.id)
    val round  = integer("round")
    val winnerId = integer("winner_id").references(Players.id).nullable()

    override val primaryKey = PrimaryKey(id)
}
