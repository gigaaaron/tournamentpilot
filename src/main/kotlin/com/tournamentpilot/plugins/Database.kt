package com.tournamentpilot.plugins

import com.tournamentpilot.models.Matches
import com.tournamentpilot.models.Players
import com.tournamentpilot.models.Tournaments
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

// ─────────────────────────────────────────────
//  Database Plugin — wires HikariCP + Exposed
// ─────────────────────────────────────────────

fun Application.configureDatabase() {
    val config = environment.config

    val hikariConfig = HikariConfig().apply {
        jdbcUrl = config.property("database.url").getString()
        driverClassName = config.property("database.driver").getString()
        username = config.property("database.user").getString()
        password = config.property("database.password").getString()
        maximumPoolSize = config.property("database.maxPoolSize").getString().toInt()
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }

    val dataSource = HikariDataSource(hikariConfig)
    Database.connect(dataSource)

    // Auto-create tables on startup (safe to run repeatedly)
    transaction {
        SchemaUtils.createMissingTablesAndColumns(Players, Tournaments, Matches)
    }

    log.info("Database connected and schema verified.")
}
