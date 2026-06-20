package com.coffeepeek.domain.model

data class City(
    val id: String,
    val name: String,
)

data class CatalogItem(
    val id: String,
    val name: String,
)

data class ShopCatalogs(
    val cities: List<City> = emptyList(),
    val beans: List<CatalogItem> = emptyList(),
    val equipment: List<CatalogItem> = emptyList(),
    val roasters: List<CatalogItem> = emptyList(),
    val brewMethods: List<CatalogItem> = emptyList(),
)

data class CreateShopInput(
    val name: String,
    val address: String,
    val cityId: String,
    val description: String? = null,
    val priceRange: Int? = null,
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,
    val instagram: String? = null,
    val equipmentIds: List<String> = emptyList(),
    val coffeeBeanIds: List<String> = emptyList(),
    val roasterIds: List<String> = emptyList(),
    val brewMethodIds: List<String> = emptyList(),
)
