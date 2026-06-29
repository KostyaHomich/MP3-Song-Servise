# MP3 Microservices — Resource Service & Song Service

## Architecture

```
┌──────────────────────┐        HTTP         ┌──────────────────────┐
│   Resource Service   │ ─────────────────► │    Song Service       │
│   :8081              │  POST /songs        │    :8082              │
│                      │  DELETE /songs?id=  │                       │
│   DB: localhost:5432  │                    │   DB: localhost:5433  │
└──────────────────────┘                     └──────────────────────┘
```

## Prerequisites

- Java 21 (JDK)
- Maven 3.9+
- Docker & Docker Compose

## Quick Start

### 1. Start the databases

```bash
docker compose up -d
```

Waits for both PostgreSQL containers to be healthy before continuing.

### 2. Build & run Song Service first

```bash
cd song-service
mvn spring-boot:run
```

Song Service listens on **http://localhost:8082**

### 3. Build & run Resource Service

```bash
cd resource-service
mvn spring-boot:run
```

Resource Service listens on **http://localhost:8081**

---

## API Reference

### Resource Service (port 8081)

#### Upload MP3
```
POST /resources
Content-Type: audio/mpeg
Body: <binary MP3 data>

Response 200: {"id": 1}
Response 400: {"error": "..."}  — invalid/non-MP3 file
Response 500: {"error": "..."}  — internal error
```

#### Download MP3
```
GET /resources/{id}

Response 200: <binary MP3 data>   Content-Type: audio/mpeg
Response 400: (invalid ID format)
Response 404: {"error": "Resource not found with id: X"}
```

#### Delete Resources (cascade deletes song metadata)
```
DELETE /resources?id=1,2,3

Response 200: {"ids": [1, 2]}    — only IDs that existed and were deleted
Response 400: {"error": "..."}   — bad CSV or length > 200
```

---

### Song Service (port 8082)

#### Create Song Metadata
```
POST /songs
Content-Type: application/json
{
  "id": 1,
  "name": "We are the champions",
  "artist": "Queen",
  "album": "News of the world",
  "duration": "02:59",
  "year": "1977"
}

Response 200: {"id": 1}
```

#### Get Song Metadata
```
GET /songs/{id}

Response 200: {"id":1,"name":"...","artist":"...","album":"...","duration":"02:59","year":"1977"}
Response 404: {"error": "Song not found with id: X"}
```

#### Delete Song Metadata
```
DELETE /songs?id=1,2,3

Response 200: {"ids": [1, 2]}
```

---

## Example cURL

```bash
# Upload an MP3
curl -X POST http://localhost:8081/resources \
  -H "Content-Type: audio/mpeg" \
  --data-binary @your_file.mp3

# Download it back
curl http://localhost:8081/resources/1 --output test.mp3

# Get its metadata
curl http://localhost:8082/songs/1

# Delete (cascades to song metadata)
curl -X DELETE "http://localhost:8081/resources?id=1"
```

---

## Configuration

| Property | Resource Service | Song Service |
|---|---|---|
| Port | 8081 | 8082 |
| DB host | localhost:5432 | localhost:5433 |
| DB name | resource_db | song_db |
| DB user | postgres | postgres |
| DB password | postgres | postgres |

All configurable via `src/main/resources/application.yml` in each service.
