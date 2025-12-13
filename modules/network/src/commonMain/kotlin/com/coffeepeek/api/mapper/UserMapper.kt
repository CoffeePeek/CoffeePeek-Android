package com.coffeepeek.api.mapper

import com.coffeepeek.api.model.User
import com.coffeepeek.api.model.response.AuthResp
import com.coffeepeek.api.utils.JsonExt
import kotlin.io.encoding.Base64

object UserMapper {


    fun AuthResp.toUser(): User {
        return accessToken.split(".")[1]
            .let { Base64.Default.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL).decode(it) }
            .decodeToString()
            .let { JsonExt.json.decodeFromString(it) }
    }

}