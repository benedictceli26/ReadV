package com.mnhs.readventures.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.mnhs.readventures.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

/** Unsigned Cloudinary uploader. Never add an API secret to the app. */
class CloudinaryImageUploadService(private val context: Context) {
    sealed interface Result {
        data class Success(val secureUrl: String) : Result
        data class Failure(val message: String) : Result
    }

    suspend fun upload(uri: Uri): Result = withContext(Dispatchers.IO) {
        val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
        val preset = BuildConfig.CLOUDINARY_UPLOAD_PRESET
        if (cloudName.isBlank() || preset.isBlank()) return@withContext Result.Failure("Cloudinary is not configured on this device.")
        val type = context.contentResolver.getType(uri)?.lowercase().orEmpty()
        if (type !in setOf("image/jpeg", "image/png", "image/webp")) return@withContext Result.Failure("Choose a JPG, PNG, or WEBP image.")
        val size = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
        if (size > 5L * 1024 * 1024) return@withContext Result.Failure("Images must be 5 MB or smaller.")
        runCatching {
            val boundary = "ReadVentures${System.currentTimeMillis()}"
            val connection = (URL("https://api.cloudinary.com/v1_1/$cloudName/image/upload").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"; doOutput = true; connectTimeout = 20_000; readTimeout = 30_000
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            }
            DataOutputStream(connection.outputStream).use { out ->
                fun field(name: String, value: String) { out.writeBytes("--$boundary\r\nContent-Disposition: form-data; name=\"$name\"\r\n\r\n$value\r\n") }
                field("upload_preset", preset); field("folder", "readventures")
                out.writeBytes("--$boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"cover\"\r\nContent-Type: $type\r\n\r\n")
                context.contentResolver.openInputStream(uri)?.use { it.copyTo(out) } ?: error("Unable to read selected image.")
                out.writeBytes("\r\n--$boundary--\r\n")
            }
            val body = (if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream).bufferedReader().use { it.readText() }
            if (connection.responseCode !in 200..299) error("Upload failed (${connection.responseCode}).")
            JSONObject(body).getString("secure_url")
        }.fold(onSuccess = { Result.Success(it) }, onFailure = { Result.Failure(it.message ?: "Image upload failed.") })
    }
}
