package com.coffeepeek.data.util

object JwtUtils {
    fun extractUserId(accessToken: String): String? = runCatching {
        val payload = accessToken.split('.').getOrNull(1) ?: return null
        val padded = payload.padEnd((payload.length + 3) / 4 * 4, '=')
        val json = decodeBase64Url(padded).decodeToString()
        val key = "\"sub\":\""
        val start = json.indexOf(key)
        if (start < 0) return null
        val from = start + key.length
        val end = json.indexOf('"', from)
        if (end < 0) return null
        json.substring(from, end)
    }.getOrNull()

    private fun decodeBase64Url(input: String): ByteArray {
        val table = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        val normalized = input.replace('-', '+').replace('_', '/')
        val output = mutableListOf<Byte>()
        var buffer = 0
        var bits = 0
        for (char in normalized) {
            if (char == '=') break
            val value = table.indexOf(char)
            if (value < 0) continue
            buffer = (buffer shl 6) or value
            bits += 6
            if (bits >= 8) {
                bits -= 8
                output.add(((buffer shr bits) and 0xFF).toByte())
            }
        }
        return output.toByteArray()
    }
}
