package com.voks.social.domain.repository

import android.net.Uri
import com.voks.social.core.utils.Resource

interface StorageRepository {
    // FASE 10: Ahora pedimos el bucketId para saber si es un Post o una Foto de Perfil
    suspend fun uploadMedia(uri: Uri, bucketId: String): Resource<String>
}