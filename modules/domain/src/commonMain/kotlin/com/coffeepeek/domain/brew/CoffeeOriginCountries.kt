package com.coffeepeek.domain.brew

/** Lightweight ISO country catalog for bean origin UI (no geo SDK). */
object CoffeeOriginCountries {

    data class Country(val code: String, val nameRu: String)

    val all: List<Country> = listOf(
        Country("ET", "Эфиопия"),
        Country("KE", "Кения"),
        Country("RW", "Руанда"),
        Country("BI", "Бурунди"),
        Country("UG", "Уганда"),
        Country("TZ", "Танзания"),
        Country("CO", "Колумбия"),
        Country("BR", "Бразилия"),
        Country("PE", "Перу"),
        Country("GT", "Гватемала"),
        Country("HN", "Гондурас"),
        Country("CR", "Коста-Рика"),
        Country("PA", "Панама"),
        Country("MX", "Мексика"),
        Country("NI", "Никарагуа"),
        Country("SV", "Сальвадор"),
        Country("EC", "Эквадор"),
        Country("BO", "Боливия"),
        Country("ID", "Индонезия"),
        Country("VN", "Вьетнам"),
        Country("IN", "Индия"),
        Country("YE", "Йемен"),
        Country("PG", "Папуа — Новая Гвинея"),
        Country("CN", "Китай"),
        Country("TH", "Таиланд"),
        Country("OTHER", "Другое / смесь"),
    )

    fun nameRu(code: String): String =
        all.firstOrNull { it.code.equals(code, ignoreCase = true) }?.nameRu
            ?: code.uppercase()

    /** Regional indicator flag emoji for alpha-2 codes; empty for OTHER. */
    fun flagEmoji(code: String): String {
        val normalized = code.uppercase()
        if (normalized.length != 2 || !normalized.all { it in 'A'..'Z' }) return ""
        val first = normalized[0].code - 0x41 + 0x1F1E6
        val second = normalized[1].code - 0x41 + 0x1F1E6
        return String(intArrayOf(first, second), 0, 2)
    }
}
