package com.coffeepeek.api.service

import com.coffeepeek.api.model.request.LoginReq
import com.coffeepeek.api.model.request.RegistrationReq
import com.coffeepeek.api.model.response.AuthResp
import com.coffeepeek.api.model.response.CodeResp
import com.coffeepeek.api.utils.getIsSuccessResult
import com.coffeepeek.api.utils.getNullableResult
import com.coffeepeek.api.utils.getResult
import com.coffeepeek.api.utils.getStringResult
import com.coffeepeek.api.utils.postResult
import com.coffeepeek.api.utils.setJsonBody
import io.ktor.client.HttpClient
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

class AuthService(
    private val client: HttpClient
) {

    suspend fun registrationUser(
        email: String,
        name: String,
        password: String
    ) = client.postResult("/api/Auth/register"){
        setJsonBody(RegistrationReq(email, password, name))
    }.getStringResult()

    suspend fun checkEmail(email: String) =
        client.getResult("/api/Auth/check-exists?email=$email").getIsSuccessResult()

    suspend fun authRefresh(
        refreshToken: String? = null
    ) = client.getResult("api/Auth/refresh?refreshToken=$refreshToken").getResult<AuthResp>()

    suspend fun authLogin(
        email: String,
        password: String
    ) = client.postResult("auth/login") {
        setJsonBody(LoginReq(email, password))
    }.getNullableResult<AuthResp>()
}