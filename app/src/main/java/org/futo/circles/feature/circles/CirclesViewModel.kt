package org.futo.circles.feature.circles

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import org.futo.circles.R
import org.futo.circles.core.base.SingleEventLiveData
import org.futo.circles.core.extensions.Response
import org.futo.circles.core.extensions.createResult
import org.futo.circles.core.extensions.launchBg
import org.futo.circles.core.feature.room.create.CreateRoomDataSource
import org.futo.circles.core.model.LoadingData
import org.futo.circles.core.provider.MatrixSessionProvider
import org.futo.circles.core.utils.getTimelineRoomFor
import org.matrix.android.sdk.api.session.getRoomSummary
import javax.inject.Inject

@HiltViewModel
class CirclesViewModel @Inject constructor(
    dataSource: CirclesDataSource,
    private val createRoomDataSource: CreateRoomDataSource
) : ViewModel() {

    val roomsLiveData = dataSource.getCirclesFlow().asLiveData()
    val navigateToCircleLiveData = SingleEventLiveData<Response<Pair<String, String>>>()
    val createTimelineLoadingLiveData = MutableLiveData<LoadingData>()


    fun createTimeLineIfNotExist(circleId: String) {
        launchBg {
            val result = createResult {
                var timelineId = getTimelineRoomFor(circleId)?.roomId
                if (timelineId == null) {
                    createTimelineLoadingLiveData.postValue(LoadingData(R.string.creating_timeline))
                    val name =
                        MatrixSessionProvider.getSessionOrThrow().getRoomSummary(circleId)?.name
                    timelineId = createRoomDataSource.createCircleTimeline(circleId, name)
                }
                circleId to timelineId
            }
            createTimelineLoadingLiveData.postValue(LoadingData(isLoading = false))
            navigateToCircleLiveData.postValue(result)
        }
    }

}