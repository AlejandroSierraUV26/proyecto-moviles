package com.backtor.services

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import io.ktor.http.content.*
import java.io.File

class CloudinaryService {
    private val cloudinary: Cloudinary = Cloudinary(ObjectUtils.asMap(
        "cloud_name", "doxptfz8d",
        "api_key", "457146275264685",
        "api_secret", "szApxpfE-Js5GfDkuOfGPDAVyzw"
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
