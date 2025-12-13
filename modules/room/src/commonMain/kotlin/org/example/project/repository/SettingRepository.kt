package org.example.project.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.project.model.Setting
import org.example.project.utils.JsonExt

interface SettingRepository {

    suspend fun save(model: Setting)

    suspend fun read(key: String): Setting?

    fun readFlow(key: String): Flow<Setting?>

    suspend fun readAll(): List<Setting>

    fun readAllFlow(): Flow<List<Setting>>

    suspend fun delete(key: String)

}

suspend inline fun <reified T> SettingRepository.saveSerializable(key: String, data: T) {
    val data = JsonExt.json.encodeToString(data)
    save(Setting(key, data))
}

suspend inline fun <reified T> SettingRepository.readSerializable(key: String): T? {
    val data = read(key) ?: return null
    return JsonExt.json.decodeFromString<T>(data.value)
}

inline fun <reified T> SettingRepository.readSerializableFlow(key: String): Flow<T?> {
    return readFlow(key)
        .map { it?.value }
        .map { it?.let { string -> JsonExt.json.decodeFromString<T>(string) } }
}