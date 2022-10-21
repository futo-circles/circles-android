package org.futo.circles.extensions

import android.util.Size
import android.widget.ImageView
import com.bumptech.glide.Glide
import org.futo.circles.model.MediaContentData

fun MediaContentData.loadEncryptedIntoWithAspect(imageView: ImageView, aspectRatio: Float) {
    imageView.post {
        if (fileUrl.startsWith(UriContentScheme)) {
            imageView.loadImage(fileUrl)
        } else {
            val imageWith = imageView.width
            val size = Size(imageWith, (imageWith / aspectRatio).toInt())
            imageView.loadEncryptedImage(this, size)
        }
    }
}