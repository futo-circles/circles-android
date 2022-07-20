package org.futo.circles.feature.photos.gallery

import android.content.Context
import android.net.Uri
import androidx.lifecycle.map
import com.bumptech.glide.Glide
import org.futo.circles.core.SingleEventLiveData
import org.futo.circles.core.picker.MediaType
import org.futo.circles.extensions.Response
import org.futo.circles.extensions.getUri
import org.futo.circles.extensions.launchBg
import org.futo.circles.feature.photos.preview.GalleryImageDataSource
import org.futo.circles.feature.room.LeaveRoomDataSource
import org.futo.circles.feature.timeline.BaseTimelineViewModel
import org.futo.circles.feature.timeline.data_source.SendMessageDataSource
import org.futo.circles.feature.timeline.data_source.TimelineDataSource
import org.futo.circles.model.GalleryImageListItem
import org.futo.circles.model.GalleryVideoListItem
import org.futo.circles.model.ImageContent
import org.futo.circles.model.VideoContent

class GalleryViewModel(
    private val roomId: String,
    private val isVideoAvailable: Boolean,
    timelineDataSource: TimelineDataSource,
    private val leaveRoomDataSource: LeaveRoomDataSource,
    private val sendMessageDataSource: SendMessageDataSource
) : BaseTimelineViewModel(timelineDataSource) {

    val scrollToTopLiveData = SingleEventLiveData<Unit>()
    val selectedImageUri = SingleEventLiveData<Response<Uri>>()
    val deleteGalleryLiveData = SingleEventLiveData<Response<Unit?>>()
    val galleryItemsLiveData = timelineDataSource.timelineEventsLiveData.map { list ->
        list.mapNotNull { post ->
            when (val content = post.content) {
                is ImageContent -> GalleryImageListItem(post.id, content, post.postInfo)
                is VideoContent ->
                    if (isVideoAvailable)
                        GalleryVideoListItem(post.id, content, post.postInfo)
                    else null
                else -> null
            }
        }
    }

    fun uploadMedia(uri: Uri, mediaType: MediaType) {
        sendMessageDataSource.sendMedia(roomId, uri, null, mediaType)
        scrollToTopLiveData.postValue(Unit)
    }

    fun deleteGallery() {
        launchBg { deleteGalleryLiveData.postValue(leaveRoomDataSource.deleteGallery()) }
    }

    fun getImageUri(context: Context, postId: String) = launchBg {
        GalleryImageDataSource(roomId, postId).getImageItem()?.imageContent?.let {
            val uri = Glide.with(context).asFile().load(it).submit().get().getUri(context)
            selectedImageUri.postValue(Response.Success(uri))
        }
    }
}