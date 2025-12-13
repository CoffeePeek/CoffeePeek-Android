package com.coffeepeek.api.service

import com.coffeepeek.api.model.response.FileInfoResp
import com.coffeepeek.api.model.response.NameList
import com.coffeepeek.api.utils.getByteArrayResult
import com.coffeepeek.api.utils.getResult
import com.coffeepeek.api.utils.postResult
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import java.util.UUID

class FileService(
    private val client: HttpClient
) {

    suspend fun save(
        byteArray: ByteArray,
        progress: (Float) -> Unit
    ) = client.postResult("file/upload"){
        contentType(ContentType.MultiPart.FormData)
        setBody(MultiPartFormDataContent(formData {
            append("file", byteArray, Headers.build {
                append(HttpHeaders.ContentType, ContentType.Image.Any.toString())
                append(HttpHeaders.ContentDisposition, "filename=\"${UUID.randomUUID()}\"")
            })
        }))
        onUpload { sent, length -> progress(sent.toFloat() / (length ?: 1)) }
    }.getResult<NameList>()

    suspend fun getFile(
        filename: String
    ) = client.getResult("file/$filename").getByteArrayResult()

    suspend fun getFileInfo(
        filename: String
    ) = client.getResult("file/info/$filename").getResult<FileInfoResp>()

}