package com.coffeepeek.domain.brew

import com.coffeepeek.domain.model.BrewMethod
import com.coffeepeek.domain.model.NewBrewSessionInput
import com.coffeepeek.domain.model.TasteTag

/**
 * Rule-based grind / brew tips. Pure client logic — no ML.
 * Tips are snapshotted on save so history stays stable if rules change.
 */
object BrewAdviceEngine {

    fun advise(input: NewBrewSessionInput): String {
        val tips = mutableListOf<String>()
        val tags = input.tasteTags.toSet()
        val method = input.method
        val duration = input.durationSec
        val score = input.overallScore

        when {
            method == BrewMethod.ESPRESSO && TasteTag.SOUR in tags && duration <= 25 ->
                tips += "Помол мельче или дольше экстракция"
            method == BrewMethod.ESPRESSO && TasteTag.BITTER in tags && duration >= 30 ->
                tips += "Помол крупнее или короче экстракция"
            method == BrewMethod.FILTER && (TasteTag.THIN in tags || TasteTag.SOUR in tags) ->
                tips += "Помол мельче или дольше bloom/pour"
            method == BrewMethod.MOKA && TasteTag.BITTER in tags ->
                tips += "Помол крупнее или чуть меньше дозы"
            method == BrewMethod.ESPRESSO && TasteTag.WEAK_FOAM in tags ->
                tips += "Проверьте помол, давление и свежесть молока"
            method == BrewMethod.CEZVE && TasteTag.BITTER in tags ->
                tips += "Крупнее помол или чуть ниже температура"
        }

        if (TasteTag.BALANCED in tags && (score == null || score >= 4)) {
            tips += "Отличный результат — повторите те же параметры"
        }

        if (tips.isEmpty()) {
            tips += when {
                tags.isEmpty() -> "Отметьте вкус, чтобы получить точный совет по помолу"
                else -> "Запишите помол и сравните со следующей чашкой"
            }
        }

        return tips.distinct().take(2).joinToString("\n")
    }
}
