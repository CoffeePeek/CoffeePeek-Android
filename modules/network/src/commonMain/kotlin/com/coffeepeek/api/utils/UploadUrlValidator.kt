package com.coffeepeek.api.utils

object UploadUrlValidator {

    fun requirePublicUploadUrl(url: String) {
        val host = runCatching {
            io.ktor.http.Url(url).host
        }.getOrElse {
            throw ApiException("Некорректный URL для загрузки фото")
        }
        if (
            host.endsWith(".internal", ignoreCase = true) ||
            host.equals("localhost", ignoreCase = true) ||
            host == "127.0.0.1"
        ) {
            throw ApiException(
                "Сервер вернул недоступный URL для загрузки ($host). Обратитесь в поддержку.",
            )
        }
    }
}
