package org.futo.circles.feature.timeline.post.share

import android.net.Uri

sealed class ShareableContent()

data class TextShareable(val text: String) : ShareableContent()
data class MediaShareable(val uriToFile: Uri, val mimeType: String) : ShareableContent()


