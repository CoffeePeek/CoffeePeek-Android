# CoffeePeek

Клиентское приложение экосистемы specialty-кофе: лента кофеен, карта, профиль, чек-ины и отзывы.

**Стек:** Kotlin Multiplatform · Compose Multiplatform · Android + Desktop (JVM)

| Платформа | Статус |
|-----------|--------|
| Android | основная, полный функционал |
| Desktop (JVM) | лента, профиль, формы; карта и Google OAuth — заглушки |

---

## Возможности

- Регистрация и вход (email/пароль, Google Sign-In на Android)
- Лента кофеен с пагинацией
- Карта с Yandex MapKit (Android)
- Карточка кофейни: фото, контакты, расписание, отзывы
- Избранное, чек-ины (публичные и приватные), создание и редактирование отзывов
- Профиль: аватар, статистика, тема оформления
- Добавление кофейни (многошаговая форма с модерацией)
- Community — в разработке (empty state)

---

## Быстрый старт

### Требования

- **JDK 17** (обязательно; JDK 24/26 ломают сборку Gradle)
- Android Studio с SDK **36**
- Git

### Настройка

```bash
git clone <repository-url>
cd CoffeePeek-Android
cp local.properties.example local.properties
```

Заполните `local.properties` (подробнее в [local.properties.example](./local.properties.example)):

| Ключ | Назначение |
|------|------------|
| `sdk.dir` | Путь к Android SDK |
| `API_BASE_URL` | URL backend API |
| `MAPKIT_API_KEY` | Yandex MapKit (карта на Android) |
| `GOOGLE_WEB_CLIENT_ID` | Google Sign-In (опционально) |

Полная инструкция для контрибьюторов: **[CONTRIBUTING.md](./CONTRIBUTING.md)**

### Сборка и запуск

**Android**

```bash
./gradlew :composeApp:assembleDebug
```

**Desktop**

```bash
./gradlew :composeApp:run
```

Hot reload (desktop):

```bash
./gradlew :composeApp:hotRunJvm --auto
```

---

## Структура проекта

```
CoffeePeek-Android/
├── composeApp/              UI, ViewModel, навигация, тема, Koin
│   └── src/
│       ├── commonMain/      общий код Compose
│       ├── androidMain/     MapKit, Google Auth, Android-специфика
│       └── jvmMain/         Desktop-специфика
├── modules/
│   ├── domain/              модели и интерфейсы репозиториев
│   ├── network/             Ktor, DTO, API-сервисы
│   ├── data/                реализации репозиториев
│   └── room/                SQLite (сессия, настройки)
├── buildSrc/                версии SDK, applicationId
├── CONTRIBUTING.md          гайд для разработчиков
├── LOG_ISSUES.md            известные баги из logcat-анализа
└── local.properties.example шаблон секретов
```

**Архитектура:** `Screen → ViewModel → domain.Repository → data → network → API`

**DI:** Koin (`composeApp/.../di/KoinApp.kt`, `modules/data/.../DataModule.kt`)

**Навигация:** `composeApp/.../ui/Navigator.kt`

---

## Конфигурация

- **API URL:** `local.properties` → `BuildConfig.API_BASE_URL` (Android) / `JvmAppConfig` (desktop)
- **Версия приложения:** `1.0.<git-commit-count>` из `composeApp/build.gradle.kts`
- **Application ID:** `com.coffeepeek`

---

## Известные проблемы

Список открытых багов и приоритетов: [LOG_ISSUES.md](./LOG_ISSUES.md)

---

## Полезные ссылки

- [CONTRIBUTING.md](./CONTRIBUTING.md) — онбординг, стиль кода, PR
- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
