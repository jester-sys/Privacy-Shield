package com.privacyshield.android.Component.Settings.theme


import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class AppSettingsSerializer @Inject constructor() : Serializer<AppSettings> {
    override val defaultValue: AppSettings = AppSettings()

    override suspend fun readFrom(input: InputStream): AppSettings {
        return try {
            // Debug log
            println("Reading from DataStore...")
            val bytes = input.readBytes()
            if (bytes.isEmpty()) {
                println("Empty bytes, returning default")
                return defaultValue
            }
            val result = ProtoBuf.decodeFromByteArray<AppSettings>(bytes)
            println("Successfully read: $result")
            result
        } catch (e: Exception) {
            println("Error reading from DataStore: ${e.message}")
            e.printStackTrace()
            defaultValue
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun writeTo(t: AppSettings, output: OutputStream) {
        try {
            println("Writing to DataStore: $t")
            val bytes = ProtoBuf.encodeToByteArray(t)
            output.write(bytes)
            output.flush()
            println("Successfully written")
        } catch (e: Exception) {
            println("Error writing to DataStore: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}