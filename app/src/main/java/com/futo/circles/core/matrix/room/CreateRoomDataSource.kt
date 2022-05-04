package com.futo.circles.core.matrix.room

import android.content.Context
import android.net.Uri
import com.futo.circles.BuildConfig
import com.futo.circles.R
import com.futo.circles.model.Circle
import com.futo.circles.model.CirclesRoom
import com.futo.circles.model.Timeline
import com.futo.circles.provider.MatrixSessionProvider
import org.matrix.android.sdk.api.session.room.Room
import org.matrix.android.sdk.api.session.room.model.PowerLevelsContent
import org.matrix.android.sdk.api.session.room.model.RoomDirectoryVisibility
import org.matrix.android.sdk.api.session.room.model.create.CreateRoomParams
import org.matrix.android.sdk.api.session.room.model.create.CreateRoomPreset
import org.matrix.android.sdk.api.session.room.powerlevels.Role
import org.matrix.android.sdk.api.session.room.roomSummaryQueryParams
import org.matrix.android.sdk.api.session.space.CreateSpaceParams

class CreateRoomDataSource(private val context: Context) {

    private val session by lazy { MatrixSessionProvider.currentSession }

    suspend fun createCircleWithTimeline(
        name: String? = null,
        iconUri: Uri? = null,
        inviteIds: List<String>? = null
    ): String {
        val circleId = createRoom(Circle(), name, null, iconUri, inviteIds)
        val timelineName = context.getString(R.string.timeline_name_formatter, name)
        val timelineId = createRoom(Timeline(), timelineName)
        session?.getRoom(circleId)?.let { circle -> setRelations(timelineId, circle) }
        return circleId
    }

    suspend fun createRoom(
        circlesRoom: CirclesRoom,
        name: String? = null,
        topic: String? = null,
        iconUri: Uri? = null,
        inviteIds: List<String>? = null
    ): String {
        val id = session?.createRoom(getParams(circlesRoom, name, topic, iconUri, inviteIds))
            ?: throw Exception("Can not create room")

        session?.getRoom(id)?.addTag(circlesRoom.tag, null)
        circlesRoom.parentTag?.let { tag ->
            findRoomByTag(tag)?.let { room -> setRelations(id, room) }
        }
        return id
    }

    private fun getParams(
        circlesRoom: CirclesRoom,
        name: String? = null,
        topic: String? = null,
        iconUri: Uri? = null,
        inviteIds: List<String>? = null
    ): CreateRoomParams {
        val params = if (circlesRoom.isSpace()) {
            CreateSpaceParams()
        } else {
            CreateRoomParams().apply {
                visibility = RoomDirectoryVisibility.PRIVATE
                preset = CreateRoomPreset.PRESET_PRIVATE_CHAT
                powerLevelContentOverride = PowerLevelsContent(
                    invite = Role.Moderator.value
                )
            }
        }.apply {
            circlesRoom.type?.let { this.roomType = it }
        }

        return params.apply {
            this.name = circlesRoom.nameId?.let { context.getString(it) } ?: name
            this.topic = topic
            avatarUri = iconUri
            inviteIds?.let { invitedUserIds.addAll(it) }
        }
    }

    private suspend fun setRelations(childId: String, parentRoom: Room) {
        val via = listOf(getHomeServerDomain())
        session?.spaceService()?.setSpaceParent(childId, parentRoom.roomId, true, via)
        parentRoom.asSpace()?.addChildren(childId, via, null)
    }

    private fun findRoomByTag(tag: String): Room? {
        val roomWithTagId = session?.getRoomSummaries(roomSummaryQueryParams { excludeType = null })
            ?.firstOrNull { summary -> summary.tags.firstOrNull { it.name == tag } != null }
            ?.roomId
        return roomWithTagId?.let { session?.getRoom(it) }
    }

    private fun getHomeServerDomain() = BuildConfig.MATRIX_HOME_SERVER_URL
        .substringAfter("//").replace("/", "")
}
