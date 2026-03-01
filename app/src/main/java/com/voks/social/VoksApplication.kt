package com.voks.social

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VoksApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                // Esto le enseña a Coil a leer URLs de videos y extraer una miniatura
                add(VideoFrameDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }
}