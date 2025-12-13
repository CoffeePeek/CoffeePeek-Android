package com.coffeepeek.api.utils

import io.ktor.client.request.*
import io.ktor.content.*

object CurlInterceptor {

    fun HttpRequestBuilder.asCurlString(): String {
        val method = method.value
        val url = url.buildString()
        val headers = headers.entries().joinToString(" ") {
            "-H '${it.key}: ${it.value.joinToString(", ")}'"
        }
        val body = body.let { body ->
            when (body) {
                is TextContent -> {
                    "-H 'Content-Type: ${body.contentType}; charset=${this.headers["Accept-Charset"]}'  -d '${body.text}'"
                }

                else -> ""
            }
        }

        return "cURL -X $method '$url' $headers $body"
    }


}