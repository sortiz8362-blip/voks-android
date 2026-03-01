package com.voks.social.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.voks.social.core.utils.Constants
import com.voks.social.core.utils.Resource
import com.voks.social.domain.repository.StorageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.appwrite.ID
import io.appwrite.models.InputFile
import io.appwrite.services.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

class AppwriteStorageRepository @Inject constructor(
    private val storage: Storage,
    @ApplicationContext private val context: Context // <-- CORRECCIÓN: Etiqueta de Hilt agregada
) : StorageRepository {

    override suspend fun uploadMedia(uri: Uri): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val file = uriToFile(uri) ?: return@withContext Resource.Error("No se pudo procesar el archivo multimedia")

            val inputFile = InputFile.fromFile(file)

            val uploadedFile = storage.createFile(
                bucketId = Constants.POST_IMAGES_BUCKET_ID,
                fileId = ID.unique(),
                file = inputFile
            )

            val fileUrl = "${Constants.APPWRITE_ENDPOINT}/storage/buckets/${Constants.POST_IMAGES_BUCKET_ID}/files/${uploadedFile.id}/view?project=${Constants.APPWRITE_PROJECT_ID}"

            file.delete()

            Resource.Success(fileUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Error al subir el archivo multimedia")
        }
    }

    private fun uriToFile(uri: Uri): File? {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "tmp"

        val tempFile = File(context.cacheDir, "media_upload_${System.currentTimeMillis()}.$extension")

        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(tempFile)

            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}