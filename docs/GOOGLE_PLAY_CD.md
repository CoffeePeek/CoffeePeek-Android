# Google Play: подписанный AAB и внутреннее тестирование

Workflow `.github/workflows/android-play.yml` выполняет две операции:

- при каждом push в `main` собирает подписанный release AAB, проверяет подпись и сохраняет AAB вместе с R8 mapping в GitHub Actions;
- при ручном запуске с `upload_to_play=true` загружает тот же подписанный AAB во внутренний трек Google Play.

Production этим workflow не публикуется. После проверки внутреннего релиза его можно продвинуть через Play Console.

## Первый релиз

Google Play Developer API не создаёт новое приложение и не подходит для самой первой загрузки пакета. Поэтому первый запуск выполняется так:

1. Добавьте перечисленные ниже variables и secrets в GitHub.
2. Запустите `Android App Bundle / Google Play` вручную с выключенным `upload_to_play`.
3. Скачайте artifact `coffeepeek-aab-<versionCode>` из завершившегося workflow.
4. Создайте первый internal release в Play Console и загрузите AAB вручную.
5. Завершите настройку Play App Signing. AAB должен быть подписан тем же upload key, который хранится в GitHub secrets.
6. Настройте сервисный аккаунт и после этого запускайте workflow с `upload_to_play=true`.

## GitHub variable

| Variable | Назначение |
|---|---|
| `API_BASE_URL_MAIN` | Production backend URL, который попадёт в release-сборку |

## GitHub secrets

| Secret | Назначение |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | Upload keystore целиком в base64 без переносов |
| `ANDROID_KEYSTORE_PASSWORD` | Пароль keystore |
| `ANDROID_KEY_ALIAS` | Alias upload key |
| `ANDROID_KEY_PASSWORD` | Пароль upload key |
| `GOOGLE_WEB_CLIENT_ID` | Web client ID для Google Sign-In; может быть пустым, если функция не используется |
| `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` | Полное содержимое JSON-ключа сервисного аккаунта Google Cloud |

Keystore для GitHub secret в Linux:

```bash
base64 -w 0 coffeepeek-upload.jks
```

Не добавляйте `.jks` или JSON-ключ сервисного аккаунта в Git.

## Доступ Google Play API

1. В Google Cloud включите `Google Play Android Developer API`.
2. Создайте сервисный аккаунт и JSON key.
3. В Play Console выдайте email сервисного аккаунта доступ к CoffeePeek с правом выпускать релизы во внутренний трек.
4. Сохраните содержимое JSON-файла в secret `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`.
5. Создайте GitHub Environment `google-play`. Рекомендуется включить required reviewers, чтобы каждая загрузка требовала подтверждения.

Workflow проверяет, что secret содержит JSON сервисного аккаунта, но не выводит его в лог.

## Запуск

В GitHub откройте `Actions` → `Android App Bundle / Google Play` → `Run workflow`.

- `upload_to_play=false`: только собрать и сохранить подписанный AAB;
- `upload_to_play=true`: собрать, проверить и отправить AAB во внутреннее тестирование.

Результат сборки находится в artifacts запуска. Локально аналогичный bundle создаётся командой:

```bash
ANDROID_KEYSTORE_PATH=/absolute/path/coffeepeek-upload.jks \
ANDROID_KEYSTORE_PASSWORD='…' \
ANDROID_KEY_ALIAS='…' \
ANDROID_KEY_PASSWORD='…' \
./gradlew :composeApp:bundleRelease
```

Локальный файл появится в `composeApp/build/outputs/bundle/release/`.

## Версии

Сейчас `versionCode` равен количеству Git-коммитов, а `versionName` имеет вид `1.0.<versionCode>`. Google Play требует, чтобы каждый следующий загружаемый `versionCode` был больше предыдущего. Не переписывайте историю `main` перед релизами и не запускайте загрузку из старого коммита.
