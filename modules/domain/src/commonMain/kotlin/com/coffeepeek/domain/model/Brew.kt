package com.coffeepeek.domain.model

enum class BrewMethod(val code: String, val labelRu: String) {
    ESPRESSO("espresso", "Эспрессо"),
    FILTER("filter", "Фильтр"),
    MOKA("moka", "Мока"),
    CEZVE("cezve", "Турка"),
    OTHER("other", "Другое");

    companion object {
        fun fromCode(code: String): BrewMethod =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: OTHER
    }
}

enum class RoastLevel(val code: String, val labelRu: String) {
    LIGHT("light", "Светлая"),
    MEDIUM("medium", "Средняя"),
    DARK("dark", "Тёмная");

    companion object {
        fun fromCode(code: String): RoastLevel =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: MEDIUM
    }
}

enum class TasteTag(val code: String, val labelRu: String) {
    SOUR("sour", "Кисло"),
    BITTER("bitter", "Горько"),
    SWEET("sweet", "Сладко"),
    THIN("thin", "Водянисто"),
    DENSE("dense", "Плотно"),
    WEAK_FOAM("weak_foam", "Слабая пенка"),
    GOOD_FOAM("good_foam", "Хорошая пенка"),
    BALANCED("balanced", "Сбалансировано");

    companion object {
        fun fromCode(code: String): TasteTag? =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) }

        fun parseList(raw: String?): List<TasteTag> {
            if (raw.isNullOrBlank()) return emptyList()
            return raw.split(',')
                .mapNotNull { fromCode(it.trim()) }
                .distinct()
        }

        fun encodeList(tags: List<TasteTag>): String =
            tags.map { it.code }.distinct().joinToString(",")
    }
}

data class BeanBag(
    val id: String,
    val name: String,
    val originCountryCode: String,
    val roastLevel: RoastLevel,
    val roasterName: String = "",
    val notes: String = "",
    val createdAt: Long,
    val updatedAt: Long,
)

data class BrewSession(
    val id: String,
    val beanId: String?,
    val method: BrewMethod,
    val doseG: Float,
    val yieldOrWaterG: Float,
    val durationSec: Int,
    val temperatureC: Float?,
    val grindNote: String,
    val tasteTags: List<TasteTag>,
    val overallScore: Int?,
    val adviceSnapshot: String,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long,
)

data class BrewSessionDetails(
    val session: BrewSession,
    val bean: BeanBag?,
)

data class NewBeanBagInput(
    val name: String,
    val originCountryCode: String,
    val roastLevel: RoastLevel,
    val roasterName: String = "",
    val notes: String = "",
)

data class NewBrewSessionInput(
    val beanId: String?,
    val method: BrewMethod,
    val doseG: Float,
    val yieldOrWaterG: Float,
    val durationSec: Int,
    val temperatureC: Float? = null,
    val grindNote: String = "",
    val tasteTags: List<TasteTag> = emptyList(),
    val overallScore: Int? = null,
    val notes: String = "",
)

data class BrewTrends(
    val periodDays: Int,
    val sessionCount: Int,
    val averageScore: Float?,
    val methodCounts: Map<BrewMethod, Int>,
    val tasteCounts: Map<TasteTag, Int>,
    val scoreDeltaVsPrevious: Float?,
    val dominantTasteShift: String?,
)

data class OriginStat(
    val countryCode: String,
    val countryNameRu: String,
    val beanCount: Int,
    val sessionCount: Int,
)
