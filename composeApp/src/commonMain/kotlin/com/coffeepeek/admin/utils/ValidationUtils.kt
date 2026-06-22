package com.coffeepeek.admin.utils

private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
private val PHONE_REGEX = "^[+]?[\\d\\s()-]{7,20}$".toRegex()
private val URL_REGEX = "^(https?://).+".toRegex(RegexOption.IGNORE_CASE)

fun validateEmailRequired(email: String): String? = when {
    email.isBlank() -> "Введите email"
    !EMAIL_REGEX.matches(email.trim()) -> "Некорректный формат email"
    else -> null
}

fun validatePasswordRequired(password: String, minLength: Int = 6): String? = when {
    password.isBlank() -> "Введите пароль"
    password.length < minLength -> "Минимум $minLength символов в пароле"
    else -> null
}

fun validateOptionalEmail(email: String): String? {
    val value = email.trim()
    if (value.isEmpty()) return null
    return if (EMAIL_REGEX.matches(value)) null else "Некорректный формат email"
}

fun validateOptionalPhone(phone: String): String? {
    val value = phone.trim()
    if (value.isEmpty()) return null
    return if (PHONE_REGEX.matches(value)) null else "Некорректный формат телефона"
}

fun validateOptionalUrl(url: String): String? {
    val value = url.trim()
    if (value.isEmpty()) return null
    return if (URL_REGEX.matches(value)) null else "Укажите ссылку с http:// или https://"
}

fun validateOptionalInstagram(instagram: String): String? {
    val value = instagram.trim()
    if (value.isEmpty()) return null
    val normalized = value.removePrefix("@")
    return if (normalized.matches("^[A-Za-z0-9._]{2,30}$".toRegex())) null else "Некорректный Instagram"
}
