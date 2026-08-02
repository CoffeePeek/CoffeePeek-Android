package com.coffeepeek.data.session

import com.coffeepeek.room.DatabaseCore

internal expect fun createPlatformSessionSecureStore(
    database: DatabaseCore,
    platformContext: Any?,
): SessionSecureStore
