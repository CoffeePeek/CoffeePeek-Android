# Contributing to CoffeePeek

Спасибо, что хотите помочь с проектом. Это Kotlin Multiplatform-приложение (Android + Desktop JVM) для экосистемы specialty-кофе.

## Быстрый старт

### Требования

| Инструмент | Версия |
|------------|--------|
| JDK | **17** (см. `buildSrc/.../Config.kt`) |
| Android Studio | с SDK **36** |
| Git | актуальная |

> **Важно:** сборка на JDK 24/26 может падать с ошибкой `java.lang.IllegalArgumentException: 26.0.1`. Используйте JDK 17 для Gradle и IDE.

### Клонирование

```bash
git clone <repository-url>
cd CoffeePeek-Android
```

### Локальная конфигурация

1. Скопируйте шаблон секретов:

   ```bash
   cp local.properties.example local.properties
   ```

2. Заполните `local.properties`:
   - `sdk.dir` — путь к Android SDK (Studio часто добавляет сама)
   - `API_BASE_URL` — URL backend (уточните у команды)
   - `MAPKIT_API_KEY` — ключ Yandex MapKit (для карты на Android)
   - `GOOGLE_WEB_CLIENT_ID` — опционально, для Google Sign-In

3. Получите у команды:
   - тестовый аккаунт на API
   - ключи, если dev-окружение отличается от дефолтного

**Никогда не коммитьте `local.properties`** — файл в `.gitignore`.

### Сборка

Android:

```bash
./gradlew :composeApp:assembleDebug
```

Desktop (JVM):

```bash
./gradlew :composeApp:run
```

Hot reload (desktop):

```bash
./gradlew :composeApp:hotRunJvm --auto
```

---

## Архитектура

```
composeApp/           UI (Compose), ViewModel, навигация, тема, DI (Koin)
modules/domain/       доменные модели и интерфейсы репозиториев
modules/network/      Ktor HttpClient, DTO, *ApiService
modules/data/         реализации репозиториев, маппинг DTO → domain
modules/room/         SQLite (сессия, настройки)
```

**Поток:** `Screen` → `ViewModel` → `domain.Repository` → `data.*Impl` → `network.*Service` → API.

**Ключевые точки входа:**

| Что | Где |
|-----|-----|
| Koin, модули приложения | `composeApp/.../di/KoinApp.kt` |
| Сеть и репозитории | `modules/data/.../DataModule.kt` |
| Навигация | `composeApp/.../ui/Navigator.kt` |
| Base URL | `AppConfig` / `Constants.BASE_URL` |
| Android Application | `FPApplication.kt` |

**Платформы:**

- **Android** — полный функционал (карта MapKit, Google OAuth, геолокация).
- **Desktop JVM** — лента, профиль, формы; карта и Google OAuth — заглушки.

---

## Как вносить изменения

### Ветки

1. Создайте ветку от актуальной `main` (или `develop` — уточните у мейнтейнера).
2. Именование: `feature/краткое-описание`, `fix/краткое-описание`.

### Стиль кода

- Следуйте существующим паттернам в соседних файлах.
- UI: Compose Material 3, цвета из `CpColor` / `MaterialTheme`, отступы `CpDimens`.
- Строки для UI — в `composeApp/src/commonMain/composeResources/values/strings.xml`.
- Новый API: DTO в `network`, интерфейс в `domain`, реализация в `data`, регистрация в `DataModule` + `KoinApp`.
- Минимальный diff: не рефакторить несвязанный код в том же PR.

### Коммиты

Короткое сообщение на русском или английском, по смыслу изменения:

```
fix: парсинг ошибки при создании чек-ина
feat: публичный чек-ин с обязательным комментарием
```

### Pull Request

В описании PR укажите:

1. **Что сделано** и зачем.
2. **Как проверить** (шаги ручного теста).
3. **Скриншоты** — для UI-изменений.
4. **Известные ограничения** — если есть зависимость от бэкенда.

Перед отправкой:

- [ ] `./gradlew :composeApp:assembleDebug` проходит
- [ ] Проверен основной сценарий (логин → лента / карта / профиль)
- [ ] Нет секретов и `local.properties` в коммите

---

## Отладка

- В `KoinApp.kt` включён `debug = true` для HTTP — в logcat видны cURL-запросы (`CoffeePeekClient`).
- Известные проблемы и баги из продакшен-логов: [`LOG_ISSUES.md`](LOG_ISSUES.md).

---

## Модули и ответственность

| Задача | Куда класть код |
|--------|-----------------|
| Новый экран | `composeApp/.../ui/screen/` |
| ViewModel | рядом с экраном |
| REST endpoint | `modules/network/.../service/` + `model/` |
| Бизнес-контракт | `modules/domain/` |
| Маппинг и кэш | `modules/data/` |
| Локальная БД | `modules/room/` |

---

## Вопросы

Если чего-то нет в репозитории (ключи, staging URL, процесс ревью) — спросите мейнтейнера или откройте issue в трекере команды.
