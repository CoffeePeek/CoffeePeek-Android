# Brew API — Phase 2 contract (backend)

Bounded context **Brew**. Do **not** mix with CheckIns / Reviews / shop catalogs.

Client remains source of truth until sync lands. Local Room IDs become `localId` for idempotent upsert.

## Auth

Same JWT Bearer as existing CoffeePeek API.

## Resources

### Beans

```
POST   /api/Brew/beans
GET    /api/Brew/beans
GET    /api/Brew/beans/{id}
PUT    /api/Brew/beans/{id}
DELETE /api/Brew/beans/{id}
```

**Bean body**

| Field | Type | Notes |
|-------|------|-------|
| `id` | uuid? | server id (omit on create) |
| `localId` | string | client UUID; unique per user; idempotency key |
| `name` | string | required |
| `originCountryCode` | string | ISO-3166-1 alpha-2 or `OTHER` |
| `roastLevel` | `light` \| `medium` \| `dark` | |
| `roasterName` | string? | |
| `notes` | string? | |
| `updatedAt` | ISO-8601 | client/server clock; LWW sync |
| `deletedAt` | ISO-8601? | soft delete |

### Sessions

```
POST   /api/Brew/sessions
GET    /api/Brew/sessions?from=&to=&method=&origin=
GET    /api/Brew/sessions/{id}
PUT    /api/Brew/sessions/{id}
DELETE /api/Brew/sessions/{id}
```

**Session body**

| Field | Type | Notes |
|-------|------|-------|
| `id` | uuid? | server id |
| `localId` | string | client UUID; idempotency key |
| `beanLocalId` / `beanId` | string? | prefer resolving via localId on sync |
| `method` | enum | `espresso` `filter` `moka` `cezve` `other` |
| `doseG` | number | |
| `yieldOrWaterG` | number | |
| `durationSec` | int | |
| `temperatureC` | number? | |
| `grindNote` | string? | |
| `tasteTags` | string[] | see MVP contract |
| `overallScore` | int? | 1–5 |
| `adviceSnapshot` | string? | frozen client tips; server stores opaque |
| `notes` | string? | |
| `updatedAt` | ISO-8601 | |
| `deletedAt` | ISO-8601? | soft delete |

### Stats

```
GET /api/Brew/stats/summary?periodDays=7|30
GET /api/Brew/stats/origins
```

`summary`: sessionCount, averageScore, methodCounts, tasteCounts, scoreDeltaVsPrevious, dominantTasteShift  
`origins`: `[{ countryCode, beanCount, sessionCount }]`

### Sync (recommended)

```
POST /api/Brew/sync
```

Request:

```json
{
  "since": "2026-01-01T00:00:00Z",
  "beans": [ { "localId": "...", "updatedAt": "...", "...": "..." } ],
  "sessions": [ { "localId": "...", "updatedAt": "...", "...": "..." } ]
}
```

Response: merged beans/sessions changed after `since`, plus server-assigned ids mapped to `localId`.

**Rules**

1. Upsert by `(userId, localId)` — repeat POST must not create duplicates.
2. Soft delete via `deletedAt`; clients tombstone locally.
3. Conflict: last-write-wins on `updatedAt` (UTC).
4. Do **not** reuse shop review rating fields (place/service/coffee).
5. Photos later: `POST /api/Photos/brew` (separate from shop photos).

## Web (Phase 3)

Read-only history + stats on coffeepeek.by after sync exists. No timer / offline write path on web in v1 of web surface.

## Out of scope for this API

- Shop brewMethods/beans/roasters catalogs (seed UI only on client)
- Social feed of brews
- ML advice generation
