package org.futo.circles.model

import androidx.annotation.StringRes
import org.futo.circles.R
import org.matrix.android.sdk.api.session.room.model.RoomType

private const val orgPrefix = "org.futo"
private const val CIRCLES_SPACE_TAG = "$orgPrefix.space.circles"
private const val GROUPS_SPACE_TAG = "$orgPrefix.space.groups"
private const val PHOTOS_SPACE_TAG = "$orgPrefix.space.photos"
const val ROOT_SPACE_TAG = "$orgPrefix.space.root"
const val CIRCLE_TAG = "$orgPrefix.social.circle"
const val GROUP_TAG = "$orgPrefix.social.group"
const val GALLERY_TAG = "$orgPrefix.social.gallery"
const val TIMELINE_TAG = "$orgPrefix.social.timeline"

const val GROUP_TYPE = GROUP_TAG
const val GALLERY_TYPE = GALLERY_TAG
const val TIMELINE_TYPE = TIMELINE_TAG


sealed class CirclesRoom(
    @StringRes open val nameId: Int?,
    open val tag: String,
    open val parentTag: String?,
    open val type: String?
) {
    fun isSpace(): Boolean = type == RoomType.SPACE
}

data class RootSpace(
    override val nameId: Int? = R.string.root_space_name,
    override val tag: String = ROOT_SPACE_TAG,
    override val parentTag: String? = null,
    override val type: String? = RoomType.SPACE
) : CirclesRoom(nameId, tag, parentTag, type)

data class CirclesSpace(
    override val nameId: Int? = R.string.circles_space_name,
    override val tag: String = CIRCLES_SPACE_TAG,
    override val parentTag: String? = ROOT_SPACE_TAG,
    override val type: String? = RoomType.SPACE
) : CirclesRoom(nameId, tag, parentTag, type)

data class PhotosSpace(
    override val nameId: Int? = R.string.photos_space_name,
    override val tag: String = PHOTOS_SPACE_TAG,
    override val parentTag: String? = ROOT_SPACE_TAG,
    override val type: String? = RoomType.SPACE
) : CirclesRoom(nameId, tag, parentTag, type)

data class GroupsSpace(
    override val nameId: Int? = R.string.groups_space_name,
    override val tag: String = GROUPS_SPACE_TAG,
    override val parentTag: String? = ROOT_SPACE_TAG,
    override val type: String? = RoomType.SPACE
) : CirclesRoom(nameId, tag, parentTag, type)

data class Circle(
    override val nameId: Int? = null,
    override val tag: String = CIRCLE_TAG,
    override val parentTag: String? = CIRCLES_SPACE_TAG,
    override val type: String? = RoomType.SPACE
) : CirclesRoom(nameId, tag, parentTag, type)

data class Group(
    override val nameId: Int? = null,
    override val tag: String = GROUP_TAG,
    override val parentTag: String? = GROUPS_SPACE_TAG,
    override val type: String? = GROUP_TYPE
) : CirclesRoom(nameId, tag, parentTag, type)

data class Gallery(
    override val nameId: Int? = null,
    override val tag: String = GALLERY_TAG,
    override val parentTag: String? = PHOTOS_SPACE_TAG,
    override val type: String? = GALLERY_TYPE
) : CirclesRoom(nameId, tag, parentTag, type)

data class Timeline(
    override val nameId: Int? = null,
    override val tag: String = TIMELINE_TAG,
    override val parentTag: String? = null,
    override val type: String? = TIMELINE_TYPE
) : CirclesRoom(nameId, tag, parentTag, type)