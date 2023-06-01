package org.futo.circles.gallery.feature.preview

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.futo.circles.core.SingleEventLiveData
import org.futo.circles.core.extensions.launchBg
import org.futo.circles.core.model.MediaContent
import org.futo.circles.core.model.PostContentType
import org.futo.circles.core.utils.FileUtils
import org.futo.circles.feature.share.ShareableContent
import org.futo.circles.feature.timeline.post.PostOptionsDataSource

class MediaPreviewViewModel(
    private val roomId: String,
    private val eventId: String,
    private val mediaPreviewDataSource: MediaPreviewDataSource,
    private val postOptionsDataSource: PostOptionsDataSource
) : ViewModel() {

    val imageLiveData = MutableLiveData<MediaContent>()
    val videoLiveData = MutableLiveData<Pair<MediaContent, Uri>>()
    val shareLiveData = SingleEventLiveData<ShareableContent>()
    val downloadLiveData = SingleEventLiveData<Unit>()


    fun loadData(context: Context) {
        val content = (mediaPreviewDataSource.getPostContent() as? MediaContent) ?: return
        when (content.type) {
            PostContentType.IMAGE_CONTENT -> imageLiveData.postValue(content)
            PostContentType.VIDEO_CONTENT -> launchBg {
                FileUtils.downloadEncryptedFileToContentUri(context, content.mediaFileData)
                    ?.let { videoLiveData.postValue(content to it) }
            }

            else -> return
        }
    }

    fun share() {
        val content = mediaPreviewDataSource.getPostContent() ?: return
        launchBg {
            shareLiveData.postValue(postOptionsDataSource.getShareableContent(content))
        }
    }

    fun removeImage() {
        postOptionsDataSource.removeMessage(roomId, eventId)
    }

    fun save() {
        val content = mediaPreviewDataSource.getPostContent() ?: return
        launchBg {
            postOptionsDataSource.saveMediaToDevice(content)
            downloadLiveData.postValue(Unit)
        }
    }
}