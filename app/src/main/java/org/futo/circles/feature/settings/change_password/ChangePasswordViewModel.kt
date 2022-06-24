package org.futo.circles.feature.settings.change_password

import androidx.lifecycle.ViewModel
import org.futo.circles.core.SingleEventLiveData
import org.futo.circles.core.matrix.pass_phrase.create.CreatePassPhraseDataSource
import org.futo.circles.extensions.Response
import org.futo.circles.extensions.createResult
import org.futo.circles.extensions.launchBg
import org.futo.circles.provider.MatrixSessionProvider

class ChangePasswordViewModel(
    private val changePasswordDataSource: ChangePasswordDataSource,
    private val createPassPhraseDataSource: CreatePassPhraseDataSource
) : ViewModel() {

    val responseLiveData = SingleEventLiveData<Response<Unit?>>()
    val passPhraseLoadingLiveData = createPassPhraseDataSource.loadingLiveData

    fun changePassword(oldPassword: String, newPassword: String) {
        launchBg {
            when (val changePasswordResult =
                changePasswordDataSource.changePassword(oldPassword, newPassword)) {
                is Response.Error -> responseLiveData.postValue(changePasswordResult)
                is Response.Success -> createNewBackup(newPassword)
            }
        }
    }

    private suspend fun createNewBackup(newPassword: String) {
        val createBackupResult =
            createResult {
                createPassPhraseDataSource.replacePassPhraseBackup(
                    MatrixSessionProvider.currentSession?.myUserId ?: "", newPassword
                )
            }
        responseLiveData.postValue(createBackupResult)
    }
}