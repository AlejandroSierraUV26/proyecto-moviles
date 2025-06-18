package com.backtor.services

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import io.ktor.http.content.*
import java.io.File
import io.github.cdimascio.dotenv.dotenv

class CloudinaryService {
    private val dotenv = dotenv {
        ignoreIfMissing = true
    }
    private val cloudinary = Cloudinary(ObjectUtils.asMap(
        "cloud_name", dotenv["CLOUDINARY_CLOUD_NAME"] ?: System.getenv("CLOUDINARY_CLOUD_NAME"),
        "api_key", dotenv["CLOUDINARY_API_KEY"] ?: System.getenv("CLOUDINARY_API_KEY"),
        "api_secret", dotenv["CLOUDINARY_API_SECRET"] ?: System.getenv("CLOUDINARY_API_SECRET")
    ))
    suspend fun uploadImage(file: PartData.FileItem): String? {
        return try {
            val tempFile = File.createTempFile("upload_${System.currentTimeMillis()}", file.originalFileName ?: "temp")
            file.streamProvider().use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            val uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.emptyMap())
            tempFile.delete()
            uploadResult["secure_url"]?.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    fun deleteImageByUrl(url: String) {
        try {
            val publicId = url.substringAfterLast("/").substringBeforeLast(".")
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
