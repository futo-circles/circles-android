package org.futo.circles.feature.room

import org.futo.circles.core.matrix.room.RoomRelationsBuilder
import org.futo.circles.extensions.createResult
import org.futo.circles.extensions.getCurrentUserPowerLevel
import org.futo.circles.extensions.getRoomOwners
import org.futo.circles.extensions.getTimelineRoomFor
import org.futo.circles.provider.MatrixSessionProvider
import org.matrix.android.sdk.api.session.getRoom
import org.matrix.android.sdk.api.session.room.powerlevels.Role

class LeaveRoomDataSource(
    private val roomId: String,
    private val roomRelationsBuilder: RoomRelationsBuilder
) {

    private val session = MatrixSessionProvider.currentSession
    private val room = session?.getRoom(roomId)

    suspend fun leaveGroup() =
        createResult { session?.roomService()?.leaveRoom(roomId) }

    suspend fun deleteCircle() = createResult {
        room?.roomSummary()?.spaceChildren?.forEach {
            roomRelationsBuilder.removeRelations(it.childRoomId, roomId)
        }
        getTimelineRoomFor(roomId)?.let { timelineRoom ->
            timelineRoom.roomSummary()?.otherMemberIds?.forEach { memberId ->
                timelineRoom.membershipService().ban(memberId)
            }
            session?.roomService()?.leaveRoom(timelineRoom.roomId)
        }
        session?.roomService()?.leaveRoom(roomId)
    }

    suspend fun deleteGallery() = createResult {
        roomRelationsBuilder.removeFromAllParents(roomId)
        session?.roomService()?.leaveRoom(roomId)
    }

    fun isUserSingleRoomOwner(): Boolean {
        val isUserOwner = getCurrentUserPowerLevel(roomId) == Role.Admin.value
        return isUserOwner && getRoomOwners(roomId).size == 1
    }
}