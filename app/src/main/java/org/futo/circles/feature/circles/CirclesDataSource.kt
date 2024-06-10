package org.futo.circles.feature.circles

import androidx.lifecycle.asFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import org.futo.circles.core.feature.room.create.CreateRoomDataSource
import org.futo.circles.core.feature.workspace.SpacesTreeAccountDataSource
import org.futo.circles.core.model.CIRCLES_SPACE_ACCOUNT_DATA_KEY
import org.futo.circles.core.provider.MatrixSessionProvider
import org.futo.circles.core.utils.getJoinedRoomById
import org.futo.circles.core.utils.getTimelineRoomFor
import org.futo.circles.core.utils.getTimelinesLiveData
import org.futo.circles.mapping.toJoinedCircleListItem
import org.futo.circles.model.CircleInvitesNotificationListItem
import org.futo.circles.model.CircleListItem
import org.matrix.android.sdk.api.extensions.tryOrNull
import org.matrix.android.sdk.api.session.getRoomSummary
import org.matrix.android.sdk.api.session.room.model.Membership
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import javax.inject.Inject

class CirclesDataSource @Inject constructor(
    private val spacesTreeAccountDataSource: SpacesTreeAccountDataSource,
    private val createRoomDataSource: CreateRoomDataSource
) {

    fun getCirclesFlow() = combine(
        getTimelinesLiveData().asFlow(),
        MatrixSessionProvider.getSessionOrThrow().roomService().getChangeMembershipsLive().asFlow()
    ) { timelines, _ ->
        withContext(Dispatchers.IO) { buildCirclesList(timelines) }
    }.distinctUntilChanged()

    private suspend fun buildCirclesList(timelines: List<RoomSummary>): List<CircleListItem> {
        val invitesCount = timelines.filter { it.membership == Membership.INVITE }.size

        val joinedCirclesWithTimelines = getJoinedCirclesIds()
            .mapNotNull { id ->
                getJoinedRoomById(id)?.roomSummary()?.let { summary ->
                    getOrCreateTimeLineIfNotExist(summary.roomId)?.let { timelineId ->
                        summary.toJoinedCircleListItem(timelineId)
                    }
                }
            }

        val displayList = mutableListOf<CircleListItem>().apply {
            if (invitesCount > 0) add(CircleInvitesNotificationListItem(invitesCount))
            addAll(joinedCirclesWithTimelines)
        }
        return displayList
    }

    fun getJoinedCirclesIds(): List<String> {
        val circlesSpaceId = spacesTreeAccountDataSource.getRoomIdByKey(
            CIRCLES_SPACE_ACCOUNT_DATA_KEY
        ) ?: return emptyList()
        val ids = getJoinedRoomById(circlesSpaceId)?.roomSummary()?.spaceChildren
            ?.map { it.childRoomId }
            ?.filter { getJoinedRoomById(it) != null }
        return ids ?: emptyList()
    }

    private suspend fun getOrCreateTimeLineIfNotExist(circleId: String): String? = tryOrNull {
        var timelineId = getTimelineRoomFor(circleId)?.roomId
        if (timelineId == null) {
            val name =
                MatrixSessionProvider.getSessionOrThrow().getRoomSummary(circleId)?.name
            timelineId = createRoomDataSource.createCircleTimeline(circleId, name)
        }
        timelineId
    }
}