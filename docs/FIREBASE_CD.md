# Firebase CD (без Google Play Console)

Этот проект уже можно обновлять для тестировщиков через Firebase App Distribution, даже если Google Play Console еще не настроен.

## Что уже делает workflow

Файл: `.github/workflows/android-firebase-distribution.yml`

- На `pull_request` в `main` собирает `debug` APK для проверки.
- На `push` в `main` и `workflow_dispatch` собирает APK с тем же release keystore, что и обычная Android-доставка.
- На `push` в `main` и `workflow_dispatch`:
  - скачивает собранный APK,
  - отправляет его в Firebase App Distribution (если настроены секреты).

## Что нужно настроить в GitHub

### 1) Repository Variables

- `API_BASE_URL_CI` — URL вашего backend для CI-сборок.

### 2) Repository Secrets

Обязательные для авто-доставки в Firebase:

- `FIREBASE_APP_ID_ANDROID` — App ID Android-приложения в Firebase.
- `FIREBASE_SERVICE_ACCOUNT_JSON` — JSON ключ service account с доступом к App Distribution.
- `FIREBASE_TESTER_GROUPS` **или** `FIREBASE_TESTERS`:
  - `FIREBASE_TESTER_GROUPS` пример: `android-qa,product-team`
  - `FIREBASE_TESTERS` пример: `qa1@example.com,qa2@example.com`

Для подписанной Firebase-сборки также обязательны:

- `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD` — те же значения, что в обычной Android-доставке.
- `GOOGLE_WEB_CLIENT_ID` — Web OAuth client ID, который использует приложение и принимает backend.

В Google Cloud Console должен быть зарегистрирован Android OAuth client для пакета `com.coffeepeek` и SHA-1 этого release keystore. Если обычный release APK уже входит через Google, дополнительный SHA-1 для Firebase App Distribution не нужен: подпись теперь одинаковая. APK, ранее опубликованные с CI debug-ключом, может потребоваться удалить перед установкой нового APK из-за смены подписи.

## Как получить `FIREBASE_SERVICE_ACCOUNT_JSON`

1. Откройте Firebase Console вашего проекта.
2. Перейдите в настройки проекта -> Service accounts.
3. Создайте новый private key (JSON).
4. Содержимое JSON целиком сохраните в GitHub Secret `FIREBASE_SERVICE_ACCOUNT_JSON`.

Минимально необходимая роль сервисного аккаунта: доступ к Firebase App Distribution (Admin/управление релизами и тестировщиками).

## Как запускать

- Автоматически: при `push` в `main`.
- Вручную: Actions -> `Android CI + Firebase App Distribution` -> Run workflow.

> Важно: кнопка **Run workflow** в GitHub обычно доступна, когда файл workflow уже есть в `main` (default branch).
> Если workflow пока только в feature-ветке, ручного запуска может не быть — это нормально.
## Что получат тестировщики

- Ссылку на установку через Firebase App Distribution.
- Обновления будут появляться после каждой отправки сборки из workflow.

## План на будущее (когда появится Play Console)

Текущий Firebase flow можно оставить для QA, а рядом добавить второй workflow:

- сборка `release` AAB,
- публикация в Google Play Internal Testing,
- staged rollout в production.

Это даст связку:
- Firebase -> быстрые тестовые апдейты,
- Google Play -> релизы для конечных пользователей.
