package com.coffeepeek.admin.utils

const val COFFEEPEEK_SHARE_TEXT =
    "Нашёл отличное приложения, которое собирает кофейни и людей, которые любят кофе — загляни: https://coffeepeek.by"

expect object ShareHelper {
    fun shareText(text: String)
}
