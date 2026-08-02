# Barista Brew Journal — MVP Contract (v1)

Local-first Android feature. No backend sync and no web UI in v1.

## Brew methods

| Code | Label (RU) |
|------|------------|
| `espresso` | Эспрессо |
| `filter` | Фильтр (V60 / pour-over) |
| `moka` | Мока |
| `cezve` | Турка / джезва |
| `other` | Другое |

## BeanBag fields

| Field | Type | Required |
|-------|------|----------|
| `id` | string UUID | yes |
| `name` | string | yes |
| `originCountryCode` | ISO-3166-1 alpha-2 | yes |
| `roastLevel` | `light` / `medium` / `dark` | yes |
| `roasterName` | string | no |
| `notes` | string | no |
| `createdAt` | epoch ms | yes |
| `updatedAt` | epoch ms | yes |

## BrewSession fields

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `id` | string UUID | yes | |
| `beanId` | string? | no | FK to BeanBag |
| `method` | BrewMethod | yes | |
| `doseG` | float | yes | coffee dose, grams |
| `yieldOrWaterG` | float | yes | espresso yield or brew water |
| `durationSec` | int | yes | from wizard timer |
| `temperatureC` | float? | no | |
| `grindNote` | string | no | free text grind setting |
| `tasteTags` | TasteTag[] | no | multi-select |
| `overallScore` | int 1–5 | no | optional in v1 UI |
| `adviceSnapshot` | string | no | frozen tips at save time |
| `notes` | string | no | |
| `createdAt` | epoch ms | yes | |
| `updatedAt` | epoch ms | yes | |

## Taste tags

| Code | Label (RU) |
|------|------------|
| `sour` | Кисло |
| `bitter` | Горько |
| `sweet` | Сладко |
| `thin` | Водянисто |
| `dense` | Плотно |
| `weak_foam` | Слабая пенка |
| `good_foam` | Хорошая пенка |
| `balanced` | Сбалансировано |

## Advice rules (client, Russian, 1–2 tips)

Rules are evaluated in code (`BrewAdviceEngine`). Snapshot is stored on the session so history does not rewrite when rules change.

| When | Tip |
|------|-----|
| espresso + sour + duration ≤ 25s | Помол мельче или дольше экстракция |
| espresso + bitter + duration ≥ 30s | Помол крупнее или короче экстракция |
| filter + (thin or sour) | Помол мельче или дольше bloom/pour |
| moka + bitter | Помол крупнее или чуть меньше дозы |
| espresso + weak_foam | Проверьте помол, давление и свежесть молока |
| cezve + bitter | Крупнее помол или чуть ниже температура |
| balanced + score ≥ 4 | Отличный результат — повторите те же параметры |
| default (any taste, no match) | Запишите помол и сравните со следующей чашкой |

## Out of v1

- Social brew feed, ML/AI coach, Bluetooth machines
- Full bean inventory with daily gram stock
- Pressure/temperature second-by-second charts
- Backend sync / web cabinet (see `docs/BREW_API.md`)

## In scope after MVP core (same client release track)

- Trends 7/30 days
- Origin country list/map lite (no heavy geo SDK)
