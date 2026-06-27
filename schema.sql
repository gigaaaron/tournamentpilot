-- TournamentPilot Database Schema
-- Run this manually if you prefer explicit table creation
-- over Exposed's SchemaUtils.createMissingTablesAndColumns()

CREATE DATABASE IF NOT EXISTS tournamentpilot
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE tournamentpilot;

CREATE TABLE IF NOT EXISTS players (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    skill_level VARCHAR(20)  NOT NULL DEFAULT 'BEGINNER',
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS tournaments (
    id INT NOT NULL AUTO_INCREMENT,
    name   VARCHAR(100) NOT NULL,
    status VARCHAR(20)  NOT NULL DEFAULT 'IN_PROGRESS',
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS matches (
    id  INT NOT NULL AUTO_INCREMENT,
    tournament_id INT NOT NULL,
    player1_id INT NOT NULL,
    player2_id  INT NOT NULL,
    round INT NOT NULL,
    winner_id INT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (tournament_id) REFERENCES tournaments(id),
    FOREIGN KEY (player1_id) REFERENCES players(id),
    FOREIGN KEY (player2_id) REFERENCES players(id),
    FOREIGN KEY (winner_id) REFERENCES players(id)
);

-- ── Sample seed data ──────────────────────────────────────────────────────────

INSERT INTO players (name, skill_level) VALUES
    ('Lee Chong Wei', 'ADVANCED'),
    ('Lin Dan', 'ADVANCED'),
    ('Viktor Axelsen', 'ADVANCED'),
    ('Kento Momota', 'ADVANCED'),
    ('Anders Antonsen', 'INTERMEDIATE'),
    ('Shi Yuqi', 'INTERMEDIATE'),
    ('Chou Tien Chen', 'INTERMEDIATE'),
    ('Jonatan Christie', 'INTERMEDIATE');
