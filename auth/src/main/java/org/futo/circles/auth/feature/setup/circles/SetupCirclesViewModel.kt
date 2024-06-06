package org.futo.circles.auth.feature.setup.circles

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.futo.circles.auth.model.SetupCirclesListItem
import javax.inject.Inject

@HiltViewModel
class SetupCirclesViewModel @Inject constructor(
    private val setupCirclesDataSource: SetupCirclesDataSource
) : ViewModel() {

    val circlesLiveData = MutableLiveData(setupCirclesDataSource.getInitialCirclesList())

    fun setImageUriForCircle(id: String, uri: Uri) {
        val list = circlesLiveData.value?.toMutableList() ?: mutableListOf()
        list.map { item -> if (item.id == id) item.copy(uri = uri) else item }
        circlesLiveData.value = list
    }

    fun removeCircle(id: String) {
        val list = circlesLiveData.value?.toMutableList() ?: mutableListOf()
        list.removeIf { it.id == id }
        circlesLiveData.value = list
    }

    fun addCircleItem(name: String) {
        val list = circlesLiveData.value?.toMutableList() ?: mutableListOf()
        list.add(SetupCirclesListItem(name))
        circlesLiveData.value = list
    }

    fun finishCirclesSetup() {

    }


}