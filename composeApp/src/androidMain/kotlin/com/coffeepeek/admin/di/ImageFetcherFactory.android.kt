package com.coffeepeek.admin.di

import com.coffeepeek.admin.utils.CustomUrlFetcher
import io.ktor.client.HttpClient

actual fun createImageUrlFetcher(httpClient: HttpClient): CustomUrlFetcher =
    CustomUrlFetcher(compress = 100..500, client = httpClient)
