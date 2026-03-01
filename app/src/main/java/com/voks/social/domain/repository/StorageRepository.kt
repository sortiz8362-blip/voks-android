package com.voks.social.domain.repository

import android.net.Uri
import com.voks.social.core.utils.Resource

interface StorageRepository {
    // Recibe una Uri (foto o video) y devuelve la URL del archivo subido
    suspend fun uploadMedia(uri: Uri): Resource<String>
}