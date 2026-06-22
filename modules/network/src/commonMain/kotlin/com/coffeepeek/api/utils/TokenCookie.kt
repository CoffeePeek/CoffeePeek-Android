package com.coffeepeek.api.utils

import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders

fun HttpResponse.extractRefreshToken(): String? {
    val setCookies = headers.getAll(HttpHeaders.SetCookie)
        ?: headers[HttpHeaders.SetCookie]?.let { listOf(it) }
        ?: return null

    return setCookies.firstNotNullOfOrNull { header -> parseSetCookieValue(header, "refreshToken") }
}

private fun parseSetCookieValue(setCookieHeader: String, cookieName: String): String? {
    val prefix = "$cookieName="
    val candidates = buildList {
        add(setCookieHeader.trim())
        setCookieHeader.split(',').mapTo(this) { it.trim() }
    }

    for (candidate in candidates) {
        val startIndex = candidate.indexOf(prefix, ignoreCase = true)
        if (startIndex < 0) continue
        val rawValue = candidate
            .substring(startIndex + prefix.length)
            .substringBefore(';')
            .trim()
        if (rawValue.isNotBlank()) {
            return rawValue.decodePercentEncoded()
        }
    }
    return null
}

private fun String.decodePercentEncoded(): String {
    val out = StringBuilder(length)
    var index = 0
    while (index < length) {
        when (val char = this[index]) {
            '%' -> {
                if (index + 2 < length) {
                    val hex = substring(index + 1, index + 3)
                    out.append(hex.toInt(16).toChar())
                    index += 3
                } else {
                    out.append(char)
                    index++
                }
            }
            else -> {
                out.append(char)
                index++
            }
        }
    }
    return out.toString()
}
