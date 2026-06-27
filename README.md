# TournamentPilot

A single-elimination tournament bracket generator.


## Tech Stack

Language : Kotlin 1.9
Framework : Ktor 2.3 (Netty engine)
Database : MySQL 8 via Exposed ORM + HikariCP
API Docs : OpenAPI 3 / Swagger UI
Build: Gradle (Kotlin DSL)



## Quick Start

### 1. Start MySQL

```bash
# Docker (easiest)
docker run -d \
  --name tournamentpilot-db \
  -e MYSQL_ROOT_PASSWORD=password \
  -e MYSQL_DATABASE=tournamentpilot \
  -p 3306:3306 \
  mysql:8

# Or use your local MySQL instance and update application.conf
```

### 2. Configure the app

Edit `src/main/resources/application.conf`:
```hocon
database {
    url = "jdbc:mysql://localhost:3306/tournamentpilot?createDatabaseIfNotExist=true&serverTimezone=UTC"
    user = "root"
    password = "password"
}
```

The schema is **auto-created on startup** via Exposed's `SchemaUtils`. No migration tool needed.

### 3. Run

```bash
./gradlew run
```

The server starts at **http://localhost:8080**.  
Swagger UI: **http://localhost:8080/swagger**

---

## API Reference

### Register a Player
```http
POST /players
Content-Type: application/json

{
  "name": "Lee Chong Wei",
  "skillLevel": "ADVANCED"
}
```

**skillLevel**: `BEGINNER` | `INTERMEDIATE` | `ADVANCED` | `A` | `B` | `C` | `D`

---

### Generate a Tournament Bracket
```http
POST /tournaments/generate
Content-Type: application/json

{
  "name": "Spring Badminton Open",
  "playerIds": [1, 2, 3, 4, 5, 6, 7, 8]
}
```

> **Rule:** `playerIds.size` must be a power of 2 — `2, 4, 8, 16, 32…`

**Response:**
```json
{
  "tournament": { "id": 1, "name": "Spring Badminton Open", "status": "IN_PROGRESS" },
  "rounds": {
    "1": [
      {
        "id": 1, "tournamentId": 1,
        "player1Id": 3, "player1Name": "Viktor Axelsen",
        "player2Id": 7, "player2Name": "Chou Tien Chen",
        "round": 1, "winnerId": null
      }
    ]
  }
}
```

---

### Get Bracket Status
```http
GET /tournaments/{id}/matches
```

Returns all rounds and match results (null `winnerId` = not yet played).

---

## Bracket Logic

The core is in `BracketService.kt`. Three steps:

```kotlin
// 1. Validate: size must be a power of 2
require(isPowerOfTwo(playerIds.size))

// 2. Shuffle for random seeding
val players = playerRepo.findByIds(playerIds).shuffled()

// 3. Pair into matches with chunked(2)
players.chunked(2).map { (p1, p2) ->
    MatchInsert(player1Id = p1.id, player2Id = p2.id, round = 1, ...)
}
```

The bitwise power-of-two check:
```kotlin
private fun isPowerOfTwo(n: Int) = n > 0 && (n and (n - 1)) == 0
// 8 = 1000b, 7 = 0111b → 1000 AND 0111 = 0 ✓
```

---

## Running Tests

```bash
./gradlew test
```

Tests cover the pure bracket logic (no DB required):
- Correct number of matches for N players
- Shuffled list preserves all players
- Non-power-of-2 counts are rejected
- Pair destructuring works correctly


## Possible Extensions

- `PATCH /matches/{id}/winner` — Record a match result
- Advance winners to Round 2 automatically
- Skill-based seeding option on top of random shuffling
- JWT auth for tournament organizers
- WebSocket for live bracket updates
