package com.coffeepeek.admin.utils

import java.time.Instant

internal actual fun currentUtcIsoDateTime(): String = Instant.now().toString()
