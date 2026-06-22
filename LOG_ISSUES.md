# Logcat-анализ CoffeePeek (2026-06-22 19:40–19:42)

Сессия: PID `29624`, пакет `com.coffeepeek`, gateway `https://gateway-dev-1b7e.up.railway.app/`.

Статусы: `OPEN` — не исправлено, `IN_PROGRESS` — в работе, `DONE` — закрыто.

---

## Сводка

| Приоритет | Открыто | Суть |
|-----------|---------|------|
| P0 | 2 | Чек-ин 400, аватар на internal URL |
| P1 | 4 | UX/ошибки API, геолокация, jank при старте |
| P2 | 5 | Техдолг, шум SDK, debug-логи |
| INFO | 6 | Не баги / ожидаемое поведение |

**Главный блокер сессии:** создание чек-ина падает с HTTP 400 (дважды подряд).

---

## P0 — ломает функциональность

### LOG-001 · Чек-ин: `POST /api/CheckIns` → 400

**Статус:** `IN_PROGRESS` (клиент: `isPublic`, блокировка повторного чек-ина)  
**Приоритет:** P0  
**Воспроизведение:** Карта → кофейня `611c3b59-c086-44bb-a4c5-c350be4ee7d9` → «Чек-ин» → сохранить (рейтинги 5/5/5).

**Лог:**
```
19:41:46 POST /api/CheckIns
Body: {"coffeeShopId":"611c3b59-c086-44bb-a4c5-c350be4ee7d9","isPublic":false,"visitedAt":"2026-06-22T16:41:46.149897Z","rating":{"place":5,"service":5,"coffee":5}}
→ CURL 400

19:41:50 повтор
→ CURL 400
```

**Что работает рядом:** `GET /api/CheckIns` → 200 (список чек-инов открывается).

**Вероятные причины (по убыванию):**

1. **`isPublic: false`** — в `ShopDetailViewModel` явно передаётся `isPublic = false`, тогда как доменная модель по умолчанию `true`. Бэкенд может не принимать приватные чек-ины или валидировать иначе.
   - Файл: `composeApp/.../shop/ShopDetailViewModel.kt`

2. **Повторный чек-ин** — кнопка чек-ина не отключается при `details.isVisited == true`. Если пользователь уже отмечался в этой кофейне, API может отвечать 400.
   - Файлы: `ShopDetailScreen.kt`, `ShopDetailViewModel.kt`

3. **Контракт рейтинга** — отправляется `rating` без `note`. Возможно, API требует заметку вместе с оценкой, или наоборот — чек-ин без рейтинга, а рейтинг только в отзыве.

4. **Тело ошибки не читается** — клиент при HTTP 400 не парсит `ApiResponse.message`, пользователь видит только «Не удалось сохранить чек-ин (400)».
   - Файл: `modules/network/.../CheckInApiService.kt`

**План исправления (клиент):**
- [ ] Парсить тело ответа при `!isSuccess()` (как в `FavoriteApiService` / `UserApiService`) и показывать `message` из API.
- [ ] Попробовать `isPublic = true` или добавить переключатель в `CheckInBottomSheet`.
- [ ] Скрыть/заблокировать чек-ин, если `details.isVisited`.
- [ ] Сверить контракт с бэкендом (Swagger / исходники `CheckInsController`).

**План (бэкенд, если клиент ок):**
- [ ] Вернуть в 400 понятный `message` («уже отмечено», «isPublic недопустим» и т.д.).

---

### LOG-002 · Аватар: presigned URL на `bucket.railway.internal`

**Статус:** `OPEN`  
**Приоритет:** P0  
**Воспроизведение:** Профиль → редактирование → выбор фото (~741 KB).

**Лог:**
```
19:41:25 POST /api/Photos/avatar → 200
19:41:25 PUT https://bucket.railway.internal:9000/coffee.avatars/...?...&content-type=Minio.DataModel.Args.PresignedPutObjectArgs&...
(статус PUT в логе не появился — вероятен timeout / DNS failure)
```

**Проблема:** устройство не может достучаться до `*.railway.internal` — это внутренний hostname Railway/MinIO. Загрузка с телефона невозможна.

**Доп. признак бага бэкенда:** в query presigned URL `content-type=Minio.DataModel.Args.PresignedPutObjectArgs` вместо `image/jpeg`.

**Файлы клиента (корректны, но зависят от URL):**
- `modules/network/.../PhotoApiService.kt`
- `modules/data/.../PhotoRepositoryImpl.kt`
- `composeApp/.../editprofile/EditProfileViewModel.kt`

**План исправления:**
- [ ] **Бэкенд:** отдавать публичный presigned URL (внешний endpoint S3/MinIO) и корректный `Content-Type`.
- [ ] **Клиент (временный workaround):** если URL содержит `railway.internal`, показывать явную ошибку «Сервер вернул недоступный URL загрузки» вместо молчаливого зависания.
- [ ] Убедиться, что `uploadClient` логирует и пробрасывает ошибку PUT (таймаут, unknown host).

---

## P1 — UX, стабильность, диагностика

### LOG-003 · Сообщение об ошибке чек-ина неинформативное

**Статус:** `DONE`  
**Связано с:** LOG-001  

`CheckInApiService.createCheckIn` при HTTP 400 сразу бросает исключение, не читая JSON:
```kotlin
if (!response.status.isSuccess()) {
    throw ApiException("Не удалось сохранить чек-ин (${response.status.value})")
}
```

**Исправление:** парсить `ApiResponse<*>` из тела даже при 4xx (паттерн уже есть в `AuthService`, `UserApiService`).

---

### LOG-004 · Кнопка чек-ина доступна для уже посещённых кофеен

**Статус:** `DONE`  
**Связано с:** LOG-001  

`ShopHeroActions` всегда `enabled = !isCheckInLoading`, не смотрит на `details.isVisited`. Бейдж «Посещено» показывается, но действие не блокируется.

**Исправление:** `enabled = !isCheckInLoading && !details.isVisited`, опционально — другой UI (disabled / «Уже отмечено»).

---

### LOG-005 · Карта: стартовая область — Минск (53.92°N, 27.58°E)

**Статус:** `OPEN` (нужна проверка на устройстве)  

**Лог:**
```
19:40:55 GET /api/Map?minLat=53.9204...&minLon=27.5863...&maxLat=53.9432...&maxLon=27.6035...
```

Координаты соответствуют центру Минска. Если пользователь не в Минске — геолокация не применилась (нет разрешения, `lastKnownLocation()` вернул `null`, или эмулятор без GPS).

**Файлы:** `CoffeeMap.android.kt`, `MapViewModel.kt` (`requestMyLocation()` в `init`).

**Исправление:**
- [ ] Проверить runtime-разрешение `ACCESS_FINE_LOCATION` после `requestMyLocation`.
- [ ] Показать snackbar «Включите геолокацию» при отказе.
- [ ] Не считать багом, если пользователь реально в Минске.

---

### LOG-006 · Просадка FPS при старте (48 + 40 пропущенных кадров)

**Статус:** `OPEN`  

**Лог:** `Skipped 48 frames` / `Skipped 40 frames`, Davey ~721 ms сразу после запуска.

**Причина:** параллельно уходят 6+ запросов (каталоги ×5 + лента), тяжёлая инициализация Compose + MapKit при первом открытии карты.

**Исправление (постепенно):**
- [ ] Отложить загрузку каталогов MapScreen до открытия вкладки «Карта».
- [ ] Кэшировать каталоги между Feed/Map/AddShop.
- [ ] ProfileInstaller / baseline profiles (низкий приоритет).

---

## P2 — техдолг и шум

### LOG-007 · Debug cURL всегда включён

**Статус:** `OPEN`  

`KoinApp.kt`: `debug = true` захардкожен → все запросы в `System.out`, токены частично редактируются, но шум в logcat.

**Исправление:** `debug = BuildConfig.DEBUG` (Android) / флаг из `local.properties`.

---

### LOG-008 · SLF4J: No providers were found

**Статус:** `OPEN`  

Зависимость MapKit/других SDK тянет SLF4J без binding. На работу не влияет, засоряет лог.

**Исправление:** `slf4j-nop` или `slf4j-android` в `androidMain` dependencies.

---

### LOG-009 · Yandex MapKit: `Java object is already finalized`

**Статус:** `OPEN`  

Многократно после закрытия карты/диалогов. Возможна утечка lifecycle или ранний GC listener'ов в `CoffeeMap.android.kt`.

**Исправление:** аудит `DisposableEffect`, не держать ссылки на `MapView` после dispose; проверить повторное создание карты при навигации.

---

### LOG-010 · `InputEventReceiver: DEAD_OBJECT` при закрытии bottom sheet

**Статус:** `OPEN` (низкий)  

`19:41:06` при закрытии диалога чек-ина. Типичная гонка Compose/Window при быстром dismiss.

**Исправление:** мониторить после фикса LOG-001; если останется — проверить `ModalBottomSheet` + back handler.

---

### LOG-011 · Presigned URL shop photos — тот же риск internal host

**Статус:** `OPEN`  

Тот же механизм, что LOG-002, затронет AddShop и фото отзывов, если бэкенд везде отдаёт `railway.internal`.

**Исправление:** единый фикс на бэкенде + валидация URL на клиенте в `PhotoApiService.uploadToPresignedUrl`.

---

## INFO — не ошибки / ожидаемо

| ID | Сообщение | Комментарий |
|----|-----------|-------------|
| INFO-01 | `yandex.maps` Vulkan / locale / API key already set | Нормальные warning SDK |
| INFO-02 | `TileDataSourceLayer::invalidateMemoryCache` | Смена темы карты |
| INFO-03 | `PolylineImageAtlas` width not power of 2 | Внутренний warning MapKit |
| INFO-04 | `VibratorInfo: Invalid frequency profile` | Драйвер устройства |
| INFO-05 | `HWUI: Format: 4 doesn't support gainmap` | Декодирование JPEG аватара |
| INFO-06 | Все остальные `CURL 200` | API в целом доступен, auth работает |

---

## Что в сессии работало нормально

- Авторизация / `GET /api/Users/me` → 200  
- Лента `GET /api/CoffeeShops` → 200  
- Карта `GET /api/Map` → 200  
- Детали кофейни → 200  
- Избранное → 200 (медленно ~1 с, но успешно)  
- Список чек-инов → 200  
- Сборка после фикса `BuildConfig` import — приложение запускается  

---

## Порядок закрытия (рекомендуемый)

1. **LOG-001** + **LOG-003** + **LOG-004** — чек-ин (клиент)  
2. **LOG-002** + **LOG-011** — загрузка фото (бэкенд + понятная ошибка на клиенте)  
3. **LOG-005** — геолокация (если подтвердится на устройстве)  
4. **LOG-006**, **LOG-007**, **LOG-008**, **LOG-009** — качество и техдолг  

---

## Чеклист верификации после фиксов

- [ ] Чек-ин новой кофейни → 200, бейдж «Посещено», запись в «Мои посещения»  
- [ ] Повторный чек-ин в той же кофейне → UI блокирует или API даёт понятную ошибку  
- [ ] Чек-ин с заметкой и без — оба сценария  
- [ ] Загрузка аватара → PUT на публичный URL → 200 → аватар в профиле  
- [ ] Карта при первом открытии центрируется на текущей позиции (с разрешением GPS)  
- [ ] В release-сборке нет cURL в logcat  

---

*Файл создан по logcat-сессии от 2026-06-22. Обновляй статусы по мере исправлений.*
