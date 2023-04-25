package org.futo.circles.feature.photos.backup

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.futo.circles.core.SingleEventLiveData
import org.futo.circles.extensions.Response
import org.futo.circles.extensions.launchBg
import org.futo.circles.model.MediaBackupSettingsData
import org.futo.circles.model.MediaFolderListItem

class MediaBackupViewModel(
    private val dataSource: MediaBackupDataSource
) : ViewModel() {

    val mediaFolderLiveData = SingleEventLiveData<List<MediaFolderListItem>>()
    val saveBackupSettingsResultLiveData = SingleEventLiveData<Response<Unit?>>()
    val initialBackupSettingsLiveData = SingleEventLiveData<MediaBackupSettingsData>()
    val isSettingsDataChangedLiveData = MutableLiveData(false)
    private val selectedFoldersIds = mutableSetOf<String>()

    init {
        getInitialBackupSettings()
    }

    private fun getInitialBackupSettings() {
        val data = dataSource.getInitialBackupSettings()
        initialBackupSettingsLiveData.value = data
        selectedFoldersIds.addAll(data.folders)
        launchBg { mediaFolderLiveData.postValue(dataSource.getAllMediaFolders(selectedFoldersIds.toList())) }
    }

    fun onFolderBackupCheckChanged(
        id: String,
        isSelected: Boolean,
        isBackupEnabled: Boolean,
        backupOverWifi: Boolean
    ) {
        if (isSelected) selectedFoldersIds.add(id)
        else selectedFoldersIds.remove(id)
        handleDataSettingsChanged(isBackupEnabled, backupOverWifi)
    }

    fun saveBackupSettings(isBackupEnabled: Boolean, backupOverWifi: Boolean) {
        launchBg {
            val result = dataSource.saveBackupSettings(
                MediaBackupSettingsData(
                    isBackupEnabled,
                    backupOverWifi,
                    selectedFoldersIds.toList()
                )
            )
            saveBackupSettingsResultLiveData.postValue(result)
        }
    }

    fun handleDataSettingsChanged(isBackupEnabled: Boolean, backupOverWifi: Boolean) {
        val newSettings =
            MediaBackupSettingsData(isBackupEnabled, backupOverWifi, selectedFoldersIds.toList())
        isSettingsDataChangedLiveData.value = newSettings != initialBackupSettingsLiveData.value
    }

}